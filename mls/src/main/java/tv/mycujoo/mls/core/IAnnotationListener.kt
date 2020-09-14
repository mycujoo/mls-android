package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity

interface IAnnotationListener {

    fun addOverlay(overlayEntity: OverlayEntity)
    fun removeOverlay(overlayEntity: OverlayEntity)


    fun addOrUpdateLingeringIntroOverlay(
        overlayEntity: OverlayEntity, animationPosition: Long,
        isPlaying: Boolean
    )

    fun addOrUpdateLingeringOutroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    )

    fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity)

    fun removeLingeringOverlay(overlayEntity: OverlayEntity)

    fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>)


    /**
     * clears entire screen
     */
    fun clearScreen(idList: List<String>)


}
