package tv.mycujoo.mls.tv.player

import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.core.IAnnotationListener
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper

class TvAnnotationListener(
    private val tvOverlayContainer: TvOverlayContainer,
    private val overlayViewHelper:
    OverlayViewHelper,
    private val downloaderClient: DownloaderClient
) : IAnnotationListener {


    override fun addOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addView(
                tvOverlayContainer.context,
                tvOverlayContainer,
                it
            )
        }
    }

    override fun removeOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeView(tvOverlayContainer, overlayEntity)
    }

    override fun removeOverlay(hideOverlayActionEntity: HideOverlayActionEntity) {
        overlayViewHelper.removeView(tvOverlayContainer, hideOverlayActionEntity)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addOrUpdateLingeringIntroOverlay(
                tvOverlayContainer,
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
                tvOverlayContainer,
                it,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.updateLingeringMidwayOverlay(tvOverlayContainer, it)
        }
    }

    override fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeView(tvOverlayContainer, overlayEntity)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
//        todo!
    }

    override fun clearScreen(idList: List<String>) {
//        todo!
    }

    override fun createVariable(variableEntity: SetVariableEntity) {
        overlayViewHelper.setVariable(variableEntity)
    }

    override fun incrementVariable(incrementVariableEntity: IncrementVariableEntity) {
        overlayViewHelper.incrementVariable(incrementVariableEntity)
    }
}