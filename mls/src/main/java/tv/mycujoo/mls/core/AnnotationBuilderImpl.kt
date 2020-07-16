package tv.mycujoo.mls.core

import okhttp3.*
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.domain.entity.SvgData
import tv.mycujoo.mls.manager.ViewIdentifierManager
import java.io.IOException

class AnnotationBuilderImpl(
    private val listener: AnnotationListener,
    private val okHttpClient: OkHttpClient,
    private val viewIdentifierManager: ViewIdentifierManager
) : AnnotationBuilder() {

    /**region Fields*/
    private var currentTime: Long = 0L
    private var isPlaying: Boolean = false

    private var overlayObjects = ArrayList<OverlayObject>()
    private var toBeDownloadedSvgList = ArrayList<String>()

    /**endregion */


    // re-write
    override fun addOverlayObjects(overlayObject: List<OverlayObject>) {
        overlayObjects.addAll(overlayObject)
    }


    override fun buildCurrentTimeRange() {
        overlayObjects.filter {
            isNotDownloading(it)
                    && isNotAttached(it)
                    && additionIsInCurrentTimeRange(it)
        }
            .forEach { overlayObject ->
                downloadSVGThenCallListener(overlayObject) { listener.onNewOverlay(it) }
            }

        overlayObjects.filter {
            isNotInDisplayingOutroAnimation(it) && isAttached(it) && removalIsInCurrentTimeRange(
                it
            )
        }.forEach {
            listener.onRemovalOverlay(it)
        }
    }

    override fun buildLingerings() {
        overlayObjects.forEach { overlayObject ->
            when {
                isLingeringEndlessOverlay(overlayObject) -> {
                    downloadSVGThenCallListener(overlayObject) {
                        listener.onLingeringOverlay(it)
                    }
                }
                isLingeringExcludingAnimationPart(overlayObject) -> {
                    downloadSVGThenCallListener(overlayObject) {
                        listener.onLingeringOverlay(it)
                    }
                }
                isLingeringInIntroAnimation(overlayObject) -> {
                    downloadSVGThenCallListener(overlayObject) {
                        listener.onLingeringIntroOverlay(
                            it,
                            currentTime - overlayObject.introTransitionSpec.offset,
                            isPlaying
                        )
                    }
                }
                isLingeringInOutroAnimation(overlayObject) -> {
                    downloadSVGThenCallListener(overlayObject) {
                        listener.onLingeringOutroOverlay(
                            it,
                            currentTime - (overlayObject.introTransitionSpec.offset + overlayObject.outroTransitionSpec.animationDuration),
                            isPlaying
                        )
                    }
                }
            }
        }
    }

    override fun removeAll() {
        listener.clearScreen(overlayObjects.map { it.id })

    }

    /**region Over-ridden Functions*/
    override fun setCurrentTime(time: Long, playing: Boolean) {
        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time isPlaying -> $isPlaying")
        currentTime = time
        isPlaying = playing
    }


    /**endregion */


    /**region Action classifiers*/

    //re-write
    private fun isNotDownloading(overlayObject: OverlayObject): Boolean {
        return toBeDownloadedSvgList.none { it == overlayObject.id }
    }

    private fun isNotAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.attachedOverlayList.none { it == overlayObject.id }
    }

    private fun isAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.attachedOverlayList.any { it == overlayObject.id }
    }

    private fun isNotInDisplayingOutroAnimation(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.attachedAnimationList.none { it == overlayObject.id }
    }

    /**
     * return true if the action offset is now or in 1 second
     */
    private fun additionIsInCurrentTimeRange(overlayObject: OverlayObject): Boolean {
        return (overlayObject.introTransitionSpec.offset >= currentTime) && (overlayObject.introTransitionSpec.offset < currentTime + 1000L)
    }

    private fun removalIsInCurrentTimeRange(overlayObject: OverlayObject): Boolean {
        if (overlayObject.outroTransitionSpec.animationType == UNSPECIFIED) {
            return false
        }

        return (overlayObject.outroTransitionSpec.offset >= currentTime) && (overlayObject.outroTransitionSpec.offset < currentTime + 1000L)
    }


    private fun isLingeringEndlessOverlay(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        if (overlayObject.outroTransitionSpec.offset == -1L) {
            // it doesn't have ending spec
            return if (hasEnteringAnimation(overlayObject.introTransitionSpec.animationType)) {
                currentTime > overlayObject.introTransitionSpec.offset + overlayObject.introTransitionSpec.animationDuration
            } else {
                currentTime > overlayObject.introTransitionSpec.offset
            }
        }
        return false

    }

    private fun isLingeringExcludingAnimationPart(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        if (overlayObject.outroTransitionSpec.offset == -1L || overlayObject.outroTransitionSpec.animationDuration == 0L) {
            return false
        }

        var leftBound = overlayObject.introTransitionSpec.offset
        var rightBound = 0L

        if (hasEnteringAnimation(overlayObject.introTransitionSpec.animationType)) {
            leftBound =
                overlayObject.introTransitionSpec.offset + overlayObject.introTransitionSpec.animationDuration
        }

        if (hasOutroAnimation(overlayObject.outroTransitionSpec.animationType)) {
            rightBound = overlayObject.outroTransitionSpec.offset
        }

        return (currentTime > leftBound) && (currentTime < rightBound)
    }

    private fun isLingeringInIntroAnimation(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        val leftBound = overlayObject.introTransitionSpec.offset
        val rightBound =
            overlayObject.introTransitionSpec.offset + overlayObject.introTransitionSpec.animationDuration

        return (leftBound <= currentTime) && (currentTime <= rightBound)
    }

    private fun isLingeringInOutroAnimation(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        if (overlayObject.outroTransitionSpec.animationDuration == -1L || overlayObject.outroTransitionSpec.animationDuration > currentTime) {
            return false
        }

        val leftBound = overlayObject.outroTransitionSpec.offset
        val rightBound =
            overlayObject.outroTransitionSpec.offset + overlayObject.outroTransitionSpec.animationDuration

        return (leftBound <= currentTime) && (currentTime <= rightBound)
    }

    private fun hasEnteringAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            FADE_IN,
            SLIDE_FROM_LEFT,
            SLIDE_FROM_RIGHT -> {
                true
            }
            else -> false
        }
    }

    private fun hasOutroAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            FADE_OUT,
            SLIDE_TO_LEFT,
            SLIDE_TO_RIGHT -> {
                true
            }
            else -> false
        }
    }
    /**endregion */

    /**region msc*/
    private fun downloadSVGThenCallListener(
        overlayObject: OverlayObject,
        callback: (OverlayObject) -> Unit
    ) {
        toBeDownloadedSvgList.add(overlayObject.id)

        val svgUrl = overlayObject.svgData!!.svgUrl!!
        val request: Request = Request.Builder().url(svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                toBeDownloadedSvgList.remove(overlayObject.id)

                println(
                    "MLS-App AnnotationBuilderImpl - getInputStream() onFailure"
                )
            }

            override fun onResponse(call: Call, response: Response) {
                toBeDownloadedSvgList.remove(overlayObject.id)
                if (response.isSuccessful && response.body() != null) {
                    callback(
                        overlayObject.copy(
                            svgData = SvgData(svgUrl, response.body()!!.byteStream())
                        )
                    )
                }
            }
        })
    }
    /**endregion */

}