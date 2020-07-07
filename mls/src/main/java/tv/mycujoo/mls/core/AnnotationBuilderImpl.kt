package tv.mycujoo.mls.core

import okhttp3.*
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.helper.OverlayViewHelper.Companion.isRemoveOrHide
import java.io.IOException

class AnnotationBuilderImpl(
    private val listener: AnnotationListener,
    private val okHttpClient: OkHttpClient
) : AnnotationBuilder() {

    private var currentTime: Long = 0L
    private var isPlaying: Boolean = false
    private val pendingActions = ArrayList<ActionWrapper>()
    private val pendingActionEntities = ArrayList<ActionEntity>()


    override fun addPendingActionsDeprecated(actions: List<ActionWrapper>) {
        pendingActions.addAll(actions)
    }

    override fun addPendingActions(actions: List<ActionEntity>) {
        pendingActionEntities.addAll(actions)
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
        println("MLS-App AnnotationBuilderImpl - buildPendings()")


        pendingActions.filter { actionWrapper -> isInCurrentTimeRange(actionWrapper) }
            .forEach { actionWrapper ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings() for Actions"
                )
                listener.onNewActionWrapperAvailable(
                    actionWrapper
                )
            }

        pendingActionEntities.filter { actionEntity -> isInCurrentTimeRange(actionEntity) }
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

    override fun buildRemovalAnnotationsUpToCurrentTime() {
        pendingActions.filter { actionWrapper -> isInStartUpToCurrentTimeRange(actionWrapper) }
            .forEach { actionWrapper ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings() for Actions"
                )
                // dismiss
                if (isDismissingType(actionWrapper) || isRemovalType(actionWrapper)) {
//                    listener.onNewRemovalOrHidingActionAvailable(actionWrapper)
                }
            }
    }

    override fun buildLingeringAnnotations() {
        pendingActionEntities.filter { isLingeringWithNoAnimation(it) || isLingeringPostAnimation(it) }
            .forEach {

                if (it.svgUrl != null) {
                    downloadSVGThenCallListener(
                        it,
                        callback = { actionEntity ->
                            listener.onLingeringActionAvailable(
                                actionEntity
                            )
                        })
                } else {
                    listener.onLingeringActionAvailable(it)
                }
            }
    }

    override fun buildLingeringIntroAnimations(isPlaying: Boolean) {
        pendingActionEntities.filter { isLingeringDuringIntroAnimation(it) }
            .forEach { actionEntity ->

                if (actionEntity.svgUrl != null) {
                    downloadSVGThenCallListener(actionEntity) { actionEntityWithSvgData ->

                        getIntroAnimationPosition(
                            actionEntity,
                            currentTime
                        )?.let { animationPosition ->

                            listener.onLingeringIntroAnimationAvailable(
                                actionEntityWithSvgData,
                                animationPosition,
                                isPlaying
                            )
                        }
                    }
                } else {
                    listener.onNewActionAvailable(actionEntity)
                }

            }
    }

    override fun buildLingeringOutroAnimations(isPlaying: Boolean) {
        pendingActionEntities.filter { it.type == ActionType.HIDE_OVERLAY }.forEach { hideAction ->
            pendingActionEntities.firstOrNull {
                it.customId == hideAction.customId &&
                        isLingeringDuringOutroAnimation(it, hideAction)
            }
                ?.let { relatedShowAction ->
                    if (relatedShowAction.svgUrl != null) {
                        downloadSVGThenCallListener(relatedShowAction) { actionEntityWithSvgData ->

                            getOutroAnimationPosition(
                                relatedShowAction,
                                currentTime
                            )?.let { animationPosition ->

                                listener.onLingeringOutroAnimationAvailable(
                                    actionEntityWithSvgData,
                                    hideAction,
                                    animationPosition,
                                    isPlaying
                                )
                            }
                        }
                    } else {
                        listener.onNewActionAvailable(relatedShowAction)
                    }
                }
        }
    }

    override fun buildRemovalAnnotations() {
        listener.clearScreen(
            pendingActionEntities.filter { actionEntity -> actionEntity.type == ActionType.HIDE_OVERLAY }
                .mapNotNull { it.customId }
                .toList())
    }


    private fun getIntroAnimationPosition(actionEntity: ActionEntity, currentTime: Long): Long {
        return currentTime - actionEntity.offset
    }

    private fun getOutroAnimationPosition(
        relatedShowAction: ActionEntity,
        currentTime: Long
    ): Long? {
        if (relatedShowAction.duration == null) {
            return null
        }
        return currentTime - relatedShowAction.offset + relatedShowAction.duration
    }

    private fun isDismissingType(actionWrapper: ActionWrapper): Boolean {
        return when (actionWrapper.action) {
            is ShowAnnouncementOverlayAction -> {
                (actionWrapper.action as ShowAnnouncementOverlayAction).dismissible
            }
            is ShowScoreboardOverlayAction -> {
                (actionWrapper.action as ShowScoreboardOverlayAction).dismissible
            }
            else -> {
                false
            }
        }
    }

    private fun isRemovalType(actionWrapper: ActionWrapper): Boolean {
        return when (actionWrapper.action) {
            is CommandAction -> {
                isRemoveOrHide(actionWrapper.action as CommandAction)
            }
            else -> {
                false
            }
        }
    }

    private fun isLingeringWithNoAnimation(actionEntity: ActionEntity): Boolean {
        if (actionEntity.duration == null) {
            return false
        }

        if (hasEnteringAnimation(actionEntity.animationType)) {
            return false
        }

        return (actionEntity.offset < currentTime) && (actionEntity.offset + actionEntity.duration > currentTime)
    }

    private fun hasEnteringAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            NONE,
            FADE_OUT -> {
                false
            }
            FADE_IN,
            SLIDE_FROM_LEADING,
            SLIDE_FROM_TRAILING -> {
                true
            }
            else -> false
        }
    }

    private fun isLingeringPostAnimation(actionEntity: ActionEntity): Boolean {
        if (actionEntity.duration == null || actionEntity.animationDuration == null) {
            return false
        }
        return (actionEntity.offset + actionEntity.animationDuration < currentTime) && (actionEntity.offset + actionEntity.duration > currentTime)
    }

    private fun isLingeringDuringIntroAnimation(actionEntity: ActionEntity): Boolean {
        if (actionEntity.animationDuration == null) {
            return false
        }
        return (actionEntity.offset < currentTime) && (actionEntity.offset + actionEntity.animationDuration > currentTime)
    }

    private fun isLingeringDuringOutroAnimation(
        showAction: ActionEntity,
        hideAction: ActionEntity
    ): Boolean {
        if (showAction.duration == null || hideAction.animationDuration == null) {
            return false
        }
        return (showAction.offset + showAction.duration + hideAction.animationDuration > currentTime) && (showAction.offset + showAction.duration < currentTime)
    }

    private fun isInCurrentTimeRange(actionEntity: ActionEntity): Boolean {
        return (actionEntity.offset >= currentTime) && (actionEntity.offset < currentTime + 1000L)
    }

    private fun isInCurrentTimeRange(actionWrapper: ActionWrapper): Boolean {
        return (actionWrapper.offset >= currentTime) && (actionWrapper.offset < currentTime + 1000L)
    }

    private fun isInStartUpToCurrentTimeRange(actionWrapper: ActionWrapper): Boolean {
        return actionWrapper.offset < currentTime + 1000L
    }
}