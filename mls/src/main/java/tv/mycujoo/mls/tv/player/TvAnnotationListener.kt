package tv.mycujoo.mls.tv.player

import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.core.IAnnotationListener
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper

class TvAnnotationListener(
    private val overlayContainer: ConstraintLayout,
    private val overlayViewHelper:
    OverlayViewHelper,
    private val downloaderClient: DownloaderClient
) : IAnnotationListener {


    override fun addOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) {
            overlayViewHelper.addView(
                overlayContainer.context,
                overlayContainer,
                it
            )
        }
    }

    override fun removeOverlay(actionId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(overlayContainer, actionId, outroTransitionSpec)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(showOverlayAction) {
            overlayViewHelper.addOrUpdateLingeringIntroOverlay(
                overlayContainer,
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
                overlayContainer,
                downloadedShowOverlayAction,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringMidwayOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) { downloadedShowOverlayAction ->
            overlayViewHelper.updateLingeringMidwayOverlay(
                overlayContainer,
                downloadedShowOverlayAction
            )
        }
    }

    override fun removeLingeringOverlay(actionId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(overlayContainer, actionId, outroTransitionSpec)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
//        todo!
    }

    override fun clearScreen(idList: List<String>) {
//        todo!
    }
}