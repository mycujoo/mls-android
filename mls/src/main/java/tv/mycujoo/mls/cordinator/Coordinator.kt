package tv.mycujoo.mls.cordinator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import okhttp3.OkHttpClient
import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.domain.mapper.HideOverlayMapper
import tv.mycujoo.domain.mapper.ShowOverlayMapper
import tv.mycujoo.domain.usecase.GetAnnotationUseCase
import tv.mycujoo.mls.core.AnnotationBuilder
import tv.mycujoo.mls.core.AnnotationBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.TimeLineSeekBar

class Coordinator(
    private val api: Api
//    private val publisher: AnnotationPublisher
) {

    var timeLineSeekBar: TimeLineSeekBar? = null
    internal lateinit var playerViewWrapper: PlayerViewWrapper
    internal lateinit var annotationBuilder: AnnotationBuilder
    internal lateinit var seekInterruptionEventListener: Player.EventListener

    fun initialize(
        exoPlayer: SimpleExoPlayer,
        handler: Handler,
        okHttpClient: OkHttpClient
    ) {
        initEventListener(exoPlayer)

        val annotationListener = object : AnnotationListener {
            override fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper) {
                when (actionWrapper.action) {
                    is ShowAnnouncementOverlayAction -> {
                        playerViewWrapper.showAnnouncementOverLay(actionWrapper.action as ShowAnnouncementOverlayAction)
                    }
                    is ShowScoreboardOverlayAction -> {
                        playerViewWrapper.showScoreboardOverlay(actionWrapper.action as ShowScoreboardOverlayAction)
                    }
                    is CommandAction -> {
                        playerViewWrapper.executeCommand(actionWrapper.action as CommandAction)
                    }
                    else -> {
                    }
                }
            }

            override fun onNewRemovalWrapperAvailable(actionWrapper: ActionWrapper) {
                when (actionWrapper.action) {
                    is ShowAnnouncementOverlayAction -> {
                        playerViewWrapper.hideOverlay((actionWrapper.action as ShowAnnouncementOverlayAction).viewId)
                    }
                    is ShowScoreboardOverlayAction -> {
                        playerViewWrapper.hideOverlay((actionWrapper.action as ShowScoreboardOverlayAction).viewId)
                    }
                    is CommandAction -> {
                        playerViewWrapper.executeCommand(actionWrapper.action as CommandAction)
                    }
                    else -> {
                    }
                }
            }

            override fun onNewActionAvailable(actionEntity: ActionEntity) {

                when (actionEntity.type) {
                    UNKNOWN -> {
                        // do nothing
                    }
                    SHOW_OVERLAY -> {
                        playerViewWrapper.showOverlay(ShowOverlayMapper.mapToEntity(actionEntity))
                    }
                    HIDE_OVERLAY -> {
                        playerViewWrapper.hideOverlay(HideOverlayMapper.mapToEntity(actionEntity))
                    }
                }
            }
        }
//        publisher.setAnnotationListener(annotationListener)


        annotationBuilder = AnnotationBuilderImpl(annotationListener, okHttpClient)
        annotationBuilder.buildPendingAnnotationsForCurrentTime()

        annotationBuilder.addPendingActionsDeprecated(api.getActions())
        annotationBuilder.addPendingActions(GetAnnotationUseCase.result().actions)

        val runnable = object : Runnable {
            override fun run() {
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                annotationBuilder.buildPendingAnnotationsForCurrentTime()
                handler.postDelayed(this, 1000L)
            }
        }

        handler.postDelayed(runnable, 1000L)
    }

    private fun initEventListener(exoPlayer: SimpleExoPlayer) {
        seekInterruptionEventListener = object : Player.EventListener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                println("MLS-App Coordinator - onPositionDiscontinuity() reason-> $reason")
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition, exoPlayer.isPlaying)
                annotationBuilder.buildRemovalAnnotationsUpToCurrentTime()
            }
        }
        exoPlayer.addListener(seekInterruptionEventListener)
    }

    fun onPlayVideo() {
        val timeLineList = api.getTimeLineMarkers()

        val longArray = LongArray(timeLineList.size)
        val booleanArray = BooleanArray(timeLineList.size)
        timeLineList.forEachIndexed() { index, timeLineItem ->
            longArray[index] = timeLineItem.streamOffset
            booleanArray[index] = false
        }
        playerViewWrapper.addMarker(longArray, booleanArray)

    }
}