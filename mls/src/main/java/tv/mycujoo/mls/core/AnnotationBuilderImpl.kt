package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.helper.OverlayCommandHelper.Companion.isRemoveOrHide

class AnnotationBuilderImpl(private val publisher: AnnotationPublisher) : AnnotationBuilder() {

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
                publisher.onNewActionWrapperAvailable(
                    actionWrapper
                )
            }

        pendingActionEntities.filter { actionEntity -> isInCurrentTimeRange(actionEntity) }
            .forEach { actionEntity: ActionEntity ->
                publisher.onNewActionAvailable(actionEntity)
            }


    }

    override fun buildRemovalAnnotationsUpToCurrentTime() {
        pendingActions.filter { actionWrapper -> isInStartUpToCurrentTimeRange(actionWrapper) }
            .forEach { actionWrapper ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings() for Actions"
                )
                // dismiss
                if (isDismissingType(actionWrapper) || isRemovalType(actionWrapper)) {
                    publisher.onNewRemovalOrHidingActionAvailable(actionWrapper)
                }
            }
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

    private fun isInCurrentTimeRange(actionWrapper: ActionWrapper): Boolean {
        return (actionWrapper.offset >= currentTime) && (actionWrapper.offset < currentTime + 1000L)
    }

    private fun isInCurrentTimeRange(actionEntity: ActionEntity): Boolean {
        return (actionEntity.offset >= currentTime) && (actionEntity.offset < currentTime + 1000L)
    }

    private fun isInStartUpToCurrentTimeRange(actionWrapper: ActionWrapper): Boolean {
        return actionWrapper.offset < currentTime + 1000L
    }
}