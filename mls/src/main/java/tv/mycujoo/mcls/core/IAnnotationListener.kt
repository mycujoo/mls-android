package tv.mycujoo.mcls.core

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mcls.api.PlayerViewContract

interface IAnnotationListener {

    fun attachPlayer(player: PlayerViewContract)

    fun addOverlay(showOverlayAction: Action.ShowOverlayAction)
    fun removeOverlay(customId: String, outroTransitionSpec: TransitionSpec?)


    fun addOrUpdateLingeringIntroOverlay(
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    )

    fun addOrUpdateLingeringOutroOverlay(
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    )

    fun addOrUpdateLingeringMidwayOverlay(showOverlayAction: Action.ShowOverlayAction)

    fun removeLingeringOverlay(customId: String, outroTransitionSpec: TransitionSpec? = null)

    fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>)


    /**
     * clears entire screen
     */
    fun clearScreen(idList: List<String>)
}
