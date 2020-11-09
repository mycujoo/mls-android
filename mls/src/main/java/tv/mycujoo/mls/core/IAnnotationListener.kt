package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.*

interface IAnnotationListener {

    fun addOverlay(overlayEntity: OverlayEntity)
    fun removeOverlay(overlayEntity: OverlayEntity)
    fun removeOverlay(hideOverlayActionEntity: HideOverlayActionEntity)


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
    
    
    fun createVariable(variableEntity: SetVariableEntity)
    fun incrementVariable(incrementVariableEntity: IncrementVariableEntity)


}
