package tv.mycujoo.mls.tv.player

import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.core.IAnnotationListener
import tv.mycujoo.mls.helper.DownloaderClient

class TvAnnotationListener(
    private val tvOverlayContainer: TvOverlayContainer,
    private val downloaderClient: DownloaderClient
) : IAnnotationListener {
    override fun addOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
        }
    }

    override fun removeOverlay(overlayEntity: OverlayEntity) {
        TODO("Not yet implemented")
    }

    override fun addOrUpdateLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun addOrUpdateLingeringOutroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        TODO("Not yet implemented")
    }

    override fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        TODO("Not yet implemented")
    }

    override fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
        TODO("Not yet implemented")
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
        TODO("Not yet implemented")
    }

    override fun clearScreen(idList: List<String>) {
        TODO("Not yet implemented")
    }
}