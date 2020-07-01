package tv.mycujoo.mls.core

import okhttp3.*
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.helper.OverlayCommandHelper.Companion.isRemoveOrHide
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
                    downloadSVGThenCallListener(actionEntity)
                } else {
                    listener.onNewActionAvailable(actionEntity)
                }
            }


    }

    private fun downloadSVGThenCallListener(actionEntity: ActionEntity) {
        val request: Request = Request.Builder().url(actionEntity.svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println(
                    "MLS-App AnnotationBuilderImpl - getInputStream() onFailure"
                )
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful && response.body() != null) {
                    listener.onNewActionAvailable(
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

    override fun buildRemovalAnnotations() {
        listener.clearScreen(pendingActionEntities.filter { actionEntity -> actionEntity.type == ActionType.HIDE_OVERLAY }
            .mapNotNull { it.customId }
            .toList())
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