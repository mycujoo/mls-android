package tv.mycujoo.mcls.core

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mcls.helper.IDownloaderClient
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.widgets.IPlayerView
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.ui.PlayerViewContract
import javax.inject.Inject

class AnnotationListener @Inject constructor(
    private val overlayViewHelper: OverlayViewHelper,
    private val downloaderClient: IDownloaderClient
) : IAnnotationListener {

    private lateinit var mMLSPlayerView: PlayerViewContract

    override fun attachPlayer(player: PlayerViewContract) {
        this.mMLSPlayerView = player
    }

    override fun addOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) { downloadedShowOverlayAction ->
            overlayViewHelper.addView(
                mMLSPlayerView.context(),
                mMLSPlayerView.overlayHost(),
                downloadedShowOverlayAction
            )
        }
    }

    override fun removeOverlay(customId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(mMLSPlayerView.overlayHost(), customId, outroTransitionSpec)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(showOverlayAction) {
            overlayViewHelper.addOrUpdateLingeringIntroOverlay(
                mMLSPlayerView.overlayHost(),
                it,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringOutroOverlay(
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(showOverlayAction) { downloadedShowOverlayAction ->
            overlayViewHelper.addOrUpdateLingeringOutroOverlay(
                mMLSPlayerView.overlayHost(),
                downloadedShowOverlayAction,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringMidwayOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) { downloadedShowOverlayAction ->
            overlayViewHelper.addOrUpdateLingeringMidwayOverlay(
                mMLSPlayerView.overlayHost(),
                downloadedShowOverlayAction
            )
        }
    }

    override fun removeLingeringOverlay(customId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(mMLSPlayerView.overlayHost(), customId, outroTransitionSpec)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
        val player = mMLSPlayerView
        if (player is MLSPlayerView) player.setTimelineMarker(timelineMarkerEntityList)
    }

    override fun clearScreen(idList: List<String>) {
        mMLSPlayerView.clearScreen(idList)
    }
}
