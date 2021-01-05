package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.widgets.MLSPlayerView

class AnnotationListener(
    private val MLSPlayerView: MLSPlayerView,
    private val overlayViewHelper: OverlayViewHelper,
    private val downloaderClient: IDownloaderClient
) :
    IAnnotationListener {
    override fun addOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) { downloadedShowOverlayAction ->
            overlayViewHelper.addView(
                MLSPlayerView.context,
                MLSPlayerView.overlayHost,
                downloadedShowOverlayAction
            )
        }
    }

    override fun removeOverlay(actionId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(MLSPlayerView.overlayHost, actionId, outroTransitionSpec)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(showOverlayAction) {
            overlayViewHelper.addOrUpdateLingeringIntroOverlay(
                MLSPlayerView.overlayHost,
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
                MLSPlayerView.overlayHost,
                downloadedShowOverlayAction,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringMidwayOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) { downloadedShowOverlayAction ->
            overlayViewHelper.addOrUpdateLingeringMidwayOverlay(
                MLSPlayerView.overlayHost,
                downloadedShowOverlayAction
            )
        }
    }

    override fun removeLingeringOverlay(actionId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(MLSPlayerView.overlayHost, actionId, outroTransitionSpec)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
        MLSPlayerView.setTimelineMarker(timelineMarkerEntityList)
    }

    override fun clearScreen(idList: List<String>) {
        MLSPlayerView.clearScreen(idList)
    }
}