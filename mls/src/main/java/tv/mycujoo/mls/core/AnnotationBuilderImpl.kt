package tv.mycujoo.mls.core

import okhttp3.*
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.domain.entity.SvgData
import tv.mycujoo.domain.entity.models.ActionType
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
    private val pendingShowActionEntities = ArrayList<ActionEntity>()
    private val pendingHideActionEntities = ArrayList<ActionEntity>()

    private var overlayObjects = ArrayList<OverlayObject>()

    /**endregion */


    // re-write
    override fun addOverlayObjects(overlayObject: List<OverlayObject>) {
        overlayObjects.addAll(overlayObject)
    }


    override fun buildCurrentTimeRange() {
        overlayObjects.filter { isNotAttached(it) && additionIsInCurrentTimeRange(it) }
            .forEach { overlayObject ->
                downloadSVGThenCallListener(overlayObject) { listener.onNewOverlay(it) }
            }

        overlayObjects.filter { removalIsInCurrentTimeRange(it) }.forEach {
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
    override fun addPendingShowActions(actions: List<ActionEntity>) {
        pendingShowActionEntities.addAll(actions)
    }

    override fun addPendingHideActions(actions: List<ActionEntity>) {
        pendingHideActionEntities.addAll(actions)
    }


    override fun setCurrentTime(time: Long, playing: Boolean) {
        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time isPlaying -> $isPlaying")
        currentTime = time
        isPlaying = playing
    }


    /**endregion */


    /**region Action classifiers*/

    //re-write
    private fun isNotAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.attachedOverlayList.none { it == overlayObject.id }
    }

    /**
     * return true if the action offset is now or in 1 second
     */
    private fun additionIsInCurrentTimeRange(overlayObject: OverlayObject): Boolean {
        return (overlayObject.introTransitionSpec.offset >= currentTime) && (overlayObject.introTransitionSpec.offset < currentTime + 1000L)
    }

    private fun removalIsInCurrentTimeRange(overlayObject: OverlayObject): Boolean {
        if (overlayObject.outroTransitionSpec.animationType == UNSPECIFED) {
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

//    private fun isLingeringExcludingAnimationPart(overlayObject: OverlayObject): Boolean {
//        if (overlayObject.introTransitionSpec.offset > currentTime) {
//            return false
//        }

//        if (actionEntity.duration != null && actionEntity.duration != -1L) {
//            return (actionEntity.offset <= currentTime && actionEntity.offset + actionEntity.duration >= currentTime)
//        } else {
//            // there must be HideAction for this action
//            return pendingHideActionEntities.firstOrNull { it.customId == actionEntity.customId }
//                ?.let { hideAction ->
//                    hideAction.offset > currentTime
//                } ?: false
//        }
//    }
    //


    private fun getIntroAnimationPosition(actionEntity: ActionEntity, currentTime: Long): Long {
        return currentTime - actionEntity.offset
    }

    private fun getOutroAnimationPositionFromSameAction(
        showAction: ActionEntity,
        currentTime: Long
    ): Long? {
        if (showAction.duration == null) {
            return null
        }
        return currentTime - showAction.offset + showAction.duration
    }

    private fun getOutroAnimationPositionFromHideAction(
        ShowAction: ActionEntity,
        hideAction: ActionEntity,
        currentTime: Long
    ): Long? {

        return currentTime - hideAction.offset + hideAction.introAnimationDuration
    }

    private fun isLingeringWithNoIntroAnimation(actionEntity: ActionEntity): Boolean {

        if (hasEnteringAnimation(actionEntity.introAnimationType)) {
            return false
        }

        if (actionEntity.duration == null) {
            return actionEntity.offset < currentTime
        }

        return (actionEntity.offset < currentTime) && (actionEntity.offset + actionEntity.duration > currentTime)
    }


    //    private fun isLingeringPostAnimation(actionEntity: ActionEntity): Boolean {
//        if (actionEntity.duration == null || actionEntity.animationDuration == null) {
//            return false
//        }
//        return (actionEntity.offset + actionEntity.animationDuration < currentTime) && (actionEntity.offset + actionEntity.duration > currentTime)
//    }
//
    private fun isLingeringDuringIntroAnimation(actionEntity: ActionEntity): Boolean {
        if (actionEntity.introAnimationDuration == -1L) {
            return false
        }
        return (actionEntity.offset < currentTime) && (actionEntity.offset + actionEntity.introAnimationDuration > currentTime)
    }

    //    private fun isLingeringDuringOutroAnimation(
//        showAction: ActionEntity,
//        hideAction: ActionEntity
//    ): Boolean {
//        if (showAction.duration == null || hideAction.animationDuration == null) {
//            return false
//        }
//        return (showAction.offset + showAction.duration + hideAction.animationDuration > currentTime) && (showAction.offset + showAction.duration < currentTime)
//    }
    private fun isLingeringDuringOutroAnimation(
        showAction: ActionEntity,
        hideAction: ActionEntity
    ): Boolean {
        if (showAction.duration != null && showAction.outroAnimationDuration != -1L) {
            return (showAction.offset + showAction.duration + showAction.outroAnimationDuration > currentTime) && (showAction.offset + showAction.duration < currentTime)
        }

        return (hideAction.offset + hideAction.outroAnimationDuration > currentTime) && (hideAction.offset < currentTime)

    }

    private fun isLingeringDuringOutroAnimation(
        showAction: ActionEntity
    ): Boolean {
        if (showAction.duration != null && showAction.outroAnimationDuration != -1L) {
            return (showAction.offset + showAction.duration + showAction.outroAnimationDuration > currentTime) && (showAction.offset + showAction.duration < currentTime)
        } else return false
    }

    private fun isPendingOutroAnimation(
        hideAction: ActionEntity
    ): Boolean {
        if (hideAction.outroAnimationDuration == -1L) {
            return false
        }
        return (hideAction.offset >= currentTime) && (hideAction.offset < currentTime + 1000L)
    }

    private fun isLingeringExcludingAnimationPart(actionEntity: ActionEntity): Boolean {
        if (actionEntity.offset > currentTime) {
            return false
        }

        if (actionEntity.duration != null && actionEntity.duration != -1L) {
            return (actionEntity.offset <= currentTime && actionEntity.offset + actionEntity.duration >= currentTime)
        } else {
            // there must be HideAction for this action
            return pendingHideActionEntities.firstOrNull { it.customId == actionEntity.customId }
                ?.let { hideAction ->
                    hideAction.offset > currentTime
                } ?: false
        }
    }

    private fun isLingeringExcludingIntroAnimationPart(actionEntity: ActionEntity): Boolean {
        if (actionEntity.introAnimationDuration == -1L) {
            return false
        }

        if (actionEntity.offset > currentTime) {
            return false
        }

        if (actionEntity.duration != null && actionEntity.duration != -1L) {
            return (actionEntity.offset <= currentTime && actionEntity.offset + actionEntity.introAnimationDuration >= currentTime)
        } else {
            // there must be HideAction for this action
            return false
        }
    }

    /**
     * return true if the action offset is now or in 1 second
     */
    private fun isInCurrentTimeRange(actionEntity: ActionEntity): Boolean {
        return (actionEntity.offset >= currentTime) && (actionEntity.offset < currentTime + 1000L)
    }

    private fun hasEnteringAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            FADE_IN,
            SLIDE_FROM_LEADING,
            SLIDE_FROM_TRAILING -> {
                true
            }
            else -> false
        }
    }

    private fun hasOutroAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            FADE_OUT,
            SLIDE_TO_LEADING,
            SLIDE_TO_TRAILING -> {
                true
            }
            else -> false
        }
    }

    private fun hasNoRemoveActionUpToThisPoint(actionEntity: ActionEntity): Boolean {
        return pendingShowActionEntities.any { it.type == ActionType.HIDE_OVERLAY && it.customId == actionEntity.customId && it.offset < actionEntity.offset }
    }
    /**endregion */

    /**region msc*/
    private fun downloadSVGThenCallListener(
        actionEntity: ActionEntity,
        callback: (ActionEntity) -> Unit
    ) {
        val request: Request = Request.Builder().url(actionEntity.svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(
                    "MLS-App AnnotationBuilderImpl - getInputStream() onFailure"
                )
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body() != null) {
                    callback(
                        actionEntity.copy(
                            svgInputStream = response.body()!!.byteStream()
                        )
                    )
                }
            }
        })
    }

    private fun downloadSVGThenCallListener(
        overlayObject: OverlayObject,
        callback: (OverlayObject) -> Unit
    ) {
        val svgUrl = overlayObject.svgData!!.svgUrl!!
        val request: Request = Request.Builder().url(svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(
                    "MLS-App AnnotationBuilderImpl - getInputStream() onFailure"
                )
            }

            override fun onResponse(call: Call, response: Response) {
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