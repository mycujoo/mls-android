package tv.mycujoo.mcls.tv.player

import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mcls.core.IAnnotationListener
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.api.PlayerViewContract
import javax.inject.Inject

class TvAnnotationListener @Inject constructor(
    private val overlayContainer: ConstraintLayout,
    private val overlayViewHelper:
    OverlayViewHelper,
    private val downloaderClient: DownloaderClient
) : IAnnotationListener {

    private lateinit var mPlayerViewContract: PlayerViewContract

    override fun attachPlayer(player: PlayerViewContract) {
        this.mPlayerViewContract = player
    }

    override fun addOverlay(showOverlayAction: Action.ShowOverlayAction) {
        downloaderClient.download(showOverlayAction) {
            overlayViewHelper.addView(
                overlayContainer.context,
                overlayContainer,
                it
            )
        }
    }

    override fun removeOverlay(customId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(overlayContainer, customId, outroTransitionSpec)
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
            overlayViewHelper.addOrUpdateLingeringMidwayOverlay(
                overlayContainer,
                downloadedShowOverlayAction
            )
        }
    }

    override fun removeLingeringOverlay(customId: String, outroTransitionSpec: TransitionSpec?) {
        overlayViewHelper.removeView(overlayContainer, customId, outroTransitionSpec)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
//        todo!
    }

    override fun clearScreen(idList: List<String>) {
//        todo!
    }
}