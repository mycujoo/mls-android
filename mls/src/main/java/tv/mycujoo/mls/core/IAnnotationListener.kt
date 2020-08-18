package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.OverlayEntity

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

    /**
     * clears entire screen
     */
    fun clearScreen(idList: List<String>)


}
