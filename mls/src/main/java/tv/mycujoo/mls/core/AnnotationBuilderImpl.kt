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

        pendingShowActionEntities.filter { isLingering(it) }.forEach { actionEntity: ActionEntity ->
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
        pendingShowActionEntities.filter { it.type == ActionType.HIDE_OVERLAY || it.type == ActionType.SHOW_OVERLAY }
            .forEach { action ->

                if (action.type == ActionType.HIDE_OVERLAY) {
                    pendingShowActionEntities.firstOrNull {
                        it.customId == action.customId &&
//                                isPendingOutroAnimation(action)
                                isInCurrentTimeRange(action)
                    }
                        ?.let { relatedShowAction ->

                            listener.onNewOutroAnimationAvailableSeparateAction(
                                relatedShowAction,
                                action
                            )
                        }
                } else if (action.type == ActionType.SHOW_OVERLAY && isInCurrentTimeRange(action)) {
                    listener.onNewOutroAnimationAvailableSameCommand(
                        action
                    )
                }


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
//        pendingShowActionEntities.filter { it.type == ActionType.HIDE_OVERLAY }
//            .forEach { hideAction ->
//                pendingShowActionEntities.firstOrNull {
//                    it.customId == hideAction.customId &&
//                            isLingeringDuringOutroAnimation(it, hideAction)
//                }
//                    ?.let { relatedShowAction ->
//                        if (relatedShowAction.svgUrl != null) {
//                            downloadSVGThenCallListener(relatedShowAction) { actionEntityWithSvgData ->
//
//                                getOutroAnimationPositionFromSameAction(
//                                    relatedShowAction,
//                                    currentTime
//                                )?.let { animationPosition ->
//
//                                    listener.onLingeringOutroAnimationAvailable(
//                                        actionEntityWithSvgData,
//                                        hideAction,
//                                        animationPosition,
//                                        isPlaying
//                                    )
//                                }
//                            }
//                        } else {
////                        listener.onNewActionAvailable(relatedShowAction)
//                            Log.w(
//                                "AnnotationBuilderImpl",
//                                "No url svg available! [buildLingeringOutroAnimations]"
//                            )
//                        }
//                    }
//            }
    }

    override fun buildRemovalAnnotations() {
//        listener.clearScreen(
//            pendingShowActionEntities.filter { actionEntity -> actionEntity.type == ActionType.HIDE_OVERLAY }
//                .mapNotNull { it.customId }
//                .toList())

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
        relatedShowAction: ActionEntity,
        currentTime: Long
    ): Long? {
        if (relatedShowAction.duration == null) {
            return null
        }
        return currentTime - relatedShowAction.offset + relatedShowAction.duration
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
//    private fun isLingeringDuringIntroAnimation(actionEntity: ActionEntity): Boolean {
//        if (actionEntity.animationDuration == null) {
//            return false
//        }
//        return (actionEntity.offset < currentTime) && (actionEntity.offset + actionEntity.animationDuration > currentTime)
//    }

//    private fun isLingeringDuringOutroAnimation(
//        showAction: ActionEntity,
//        hideAction: ActionEntity
//    ): Boolean {
//        if (showAction.duration == null || hideAction.animationDuration == null) {
//            return false
//        }
//        return (showAction.offset + showAction.duration + hideAction.animationDuration > currentTime) && (showAction.offset + showAction.duration < currentTime)
//    }

    private fun isPendingOutroAnimation(
        hideAction: ActionEntity
    ): Boolean {
        if (hideAction.outroAnimationDuration == -1L) {
            return false
        }
        return (hideAction.offset >= currentTime) && (hideAction.offset < currentTime + 1000L)
    }

    private fun isLingering(actionEntity: ActionEntity): Boolean {
        if (actionEntity.offset > currentTime){
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