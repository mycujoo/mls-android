package tv.mycujoo.mls.core

import okhttp3.*
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.models.ActionType
import java.io.IOException

class AnnotationBuilderImpl(
    private val listener: AnnotationListener,
    private val okHttpClient: OkHttpClient
) : AnnotationBuilder() {

    /**region Fields*/
    private var currentTime: Long = 0L
    private var isPlaying: Boolean = false
    private val pendingShowActionEntities = ArrayList<ActionEntity>()
    private val pendingHideActionEntities = ArrayList<ActionEntity>()
    /**endregion */


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

    override fun buildPendingAnnotationsForCurrentTime() {
        if (!isPlaying) {
            return
        }

        pendingShowActionEntities.filter { actionEntity ->
            isInCurrentTimeRange(actionEntity)
        }
            .forEach { actionEntity: ActionEntity ->
                if (actionEntity.svgUrl != null) {
                    downloadSVGThenCallListener(actionEntity) { actionEntityWithSvgData ->
                        listener.onNewActionAvailable(
                            actionEntityWithSvgData
                        )
                    }
                } else {
                    listener.onNewActionAvailable(actionEntity)
                }
            }


    }

    override fun buildLingeringAnnotationsUpToCurrentTime() {

        pendingShowActionEntities.filter { isLingeringExcludingAnimationPart(it) }
            .forEach { actionEntity: ActionEntity ->
                if (actionEntity.svgUrl != null) {
                    downloadSVGThenCallListener(actionEntity) { actionEntityWithSvgData ->
                        listener.onLingeringActionAvailable(
                            actionEntityWithSvgData
                        )
                    }
                } else {
//                listener.onNewActionAvailable(actionEntity)
                }
            }
    }

    override fun buildPendingOutroAnimations() {
        // outro animations might be found in hide_overlay or in the same show_overlay action
        pendingHideActionEntities.forEach { hideAction ->
            pendingShowActionEntities.firstOrNull {
                it.customId == hideAction.customId && isInCurrentTimeRange(
                    hideAction
                )
            }
                ?.let { showAction ->
                    listener.onNewOutroAnimationAvailableSeparateAction(
                        showAction,
                        hideAction
                    )
                }
        }

        pendingShowActionEntities.filter {
            hasOutroAnimation(it.introAnimationType) && isInCurrentTimeRange(
                it
            )
        }.forEach {
            listener.onNewOutroAnimationAvailableSameCommand(it)
        }

    }

    override fun buildLingeringIntroAnimations(isPlaying: Boolean) {
//        pendingShowActionEntities.filter { isLingeringDuringIntroAnimation(it) }
//            .forEach { actionEntity ->
//
//                if (actionEntity.svgUrl != null) {
//                    downloadSVGThenCallListener(actionEntity) { actionEntityWithSvgData ->
//
//                        getIntroAnimationPosition(
//                            actionEntity,
//                            currentTime
//                        ).let { animationPosition ->
//
//                            listener.onLingeringIntroAnimationAvailable(
//                                actionEntityWithSvgData,
//                                animationPosition,
//                                isPlaying
//                            )
//                        }
//                    }
//                } else {
////                    listener.onNewActionAvailable(actionEntity)
//                    Log.w(
//                        "AnnotationBuilderImpl",
//                        "No url svg available! [buildLingeringIntroAnimations]"
//                    )
//                }
//
//            }
    }

    override fun buildLingeringOutroAnimations(isPlaying: Boolean) {

        // outro animations might be found in hide_overlay or in the same show_overlay action
        pendingHideActionEntities.forEach { hideAction ->
            pendingShowActionEntities.firstOrNull {
                it.customId == hideAction.customId &&
                        isLingeringDuringOutroAnimation(it, hideAction)
            }
                ?.let { showAction ->

                    if (showAction.svgUrl != null) {
                        downloadSVGThenCallListener(showAction) { showActionWithSvg ->

                            getOutroAnimationPositionFromHideAction(
                                showActionWithSvg,
                                hideAction,
                                currentTime
                            )?.let { animationProgress ->

                                listener.onLingeringOutroAnimationAvailableFromSeparateAction(
                                    showActionWithSvg,
                                    hideAction,
                                    animationProgress,
                                    isPlaying
                                )
                            }
                        }
                    }
                }
        }

        pendingShowActionEntities.filter {
            hasOutroAnimation(it.introAnimationType) && isLingeringDuringOutroAnimation(
                it
            )
        }.forEach { showAction ->

            if (showAction.svgUrl != null) {
                downloadSVGThenCallListener(showAction) { showActionWithSvg ->

                    getOutroAnimationPositionFromSameAction(
                        showActionWithSvg,
                        currentTime
                    )?.let { animationProgress ->

                        listener.onLingeringOutroAnimationAvailableFromSameAction(
                            showActionWithSvg,
                            animationProgress,
                            isPlaying
                        )
                    }
                }
            }
        }
    }

    override fun buildRemovalAnnotations() {
        listener.clearScreen(
            pendingHideActionEntities.mapNotNull { it.customId }
                .toList()
        )

        listener.clearScreen(
            pendingShowActionEntities.filter { hasOutroAnimation(it.introAnimationType) }
                .mapNotNull { it.customId }.toList()
        )
    }

    override fun buildRemovalAnnotationsUpToCurrentTime() {
        listener.clearScreen(
            pendingHideActionEntities.filter { it.offset <= currentTime }.mapNotNull { it.customId }
                .toList()
        )
        listener.clearScreen(
            pendingShowActionEntities.filter { hasOutroAnimation(it.introAnimationType) && it.offset + it.duration!! - it.outroAnimationDuration <= currentTime }
                .mapNotNull { it.customId }.toList()
        )
    }

    /**endregion */


    /**region Action classifiers*/
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
    /**endregion */

}