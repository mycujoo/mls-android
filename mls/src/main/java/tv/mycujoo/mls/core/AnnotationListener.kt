package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.widgets.MLSPlayerView

/**
 * Difference between "overlayEntity.isOnScreen = true" and "identifierManager.overlayBlueprintIsAttached(overlayEntity.id)" ?
 * Ideally they return same result for a given overlay. This is to block the very few milli second, if not nano second, that takes for Android OS to build a View.
 * This is the scenario:
 * An overlay view has to be build based on a given OverlayEntity,
 * BEFORE & WHILE new ScaffoldView or any other needed View is instantiated, the IdentifierManager returns false.
 * It only return true AFTER the view is created.
 * In that very short time, the boolean flag "isOnScreen" prevents from creating multiple overlays from single overlay entity
 */
class AnnotationListener(
    private val MLSPlayerView: MLSPlayerView,
    private val overlayViewHelper: OverlayViewHelper,
    private val downloaderClient: IDownloaderClient
) :
    IAnnotationListener {
    override fun addOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addView(
                MLSPlayerView.context,
                MLSPlayerView.overlayHost,
                it
            )
        }
    }

    override fun removeOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeView(MLSPlayerView.overlayHost, overlayEntity)
    }

    override fun removeOverlay(hideOverlayActionEntity: HideOverlayActionEntity) {
        overlayViewHelper.removeView(MLSPlayerView.overlayHost, hideOverlayActionEntity)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.addOrUpdateLingeringIntroOverlay(
                MLSPlayerView.overlayHost,
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
                MLSPlayerView.overlayHost,
                it,
                animationPosition,
                isPlaying
            )
        }
    }

    override fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            overlayViewHelper.updateLingeringMidwayOverlay(MLSPlayerView.overlayHost, it)
        }
    }

    override fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
        overlayViewHelper.removeView(MLSPlayerView.overlayHost, overlayEntity)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
        MLSPlayerView.setTimelineMarker(timelineMarkerEntityList)
    }

    override fun clearScreen(idList: List<String>) {
        MLSPlayerView.clearScreen(idList)
    }
}