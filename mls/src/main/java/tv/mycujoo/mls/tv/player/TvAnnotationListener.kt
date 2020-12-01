package tv.mycujoo.mls.tv.player

import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.core.IAnnotationListener
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper

class TvAnnotationListener(
    private val overlayContainer: ConstraintLayout,
    private val overlayViewHelper:
    OverlayViewHelper,
    private val downloaderClient: DownloaderClient
) : IAnnotationListener {


    override fun addOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addView(
                overlayContainer.context,
                overlayContainer,
                it
            )
        }
    }

    override fun removeOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeView(overlayContainer, overlayEntity)
    }

    override fun removeOverlay(hideOverlayActionEntity: HideOverlayActionEntity) {
        overlayViewHelper.removeView(overlayContainer, hideOverlayActionEntity)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addOrUpdateLingeringIntroOverlay(
                overlayContainer,
                it,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringOutroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addOrUpdateLingeringOutroOverlay(
                overlayContainer,
                it,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.updateLingeringMidwayOverlay(overlayContainer, it)
        }
    }

    override fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeView(overlayContainer, overlayEntity)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
//        todo!
    }

    override fun clearScreen(idList: List<String>) {
//        todo!
    }
}