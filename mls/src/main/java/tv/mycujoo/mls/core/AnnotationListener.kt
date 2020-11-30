package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.manager.contracts.IViewHandler
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
    private val viewHandler: IViewHandler,
    private val downloaderClient: IDownloaderClient
) :
    IAnnotationListener {
    override fun addOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            it.isOnScreen = true
            if (it.introTransitionSpec.animationType == AnimationType.NONE) {
                MLSPlayerView.onNewOverlayWithNoAnimation(it)
            } else {
                MLSPlayerView.onNewOverlayWithAnimation(it)
            }
        }

    }

    override fun removeOverlay(overlayEntity: OverlayEntity) {
        overlayEntity.isOnScreen = false
        if (overlayEntity.outroTransitionSpec.animationType == AnimationType.NONE) {
            MLSPlayerView.onOverlayRemovalWithNoAnimation(overlayEntity)
        } else {
            MLSPlayerView.onOverlayRemovalWithAnimation(overlayEntity)
        }
    }

    override fun removeOverlay(hideOverlayActionEntity: HideOverlayActionEntity) {
        MLSPlayerView.onOverlayRemovalWithNoAnimation(hideOverlayActionEntity)
    }

    override fun addOrUpdateLingeringIntroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        downloaderClient.download(overlayEntity) {
            overlayEntity.isOnScreen = true
            if (viewHandler.overlayBlueprintIsAttached(overlayEntity.id)) {
                MLSPlayerView.updateLingeringIntroOverlay(
                    overlayEntity,
                    animationPosition,
                    isPlaying
                )
            } else {
                MLSPlayerView.addLingeringIntroOverlay(
                    overlayEntity,
                    animationPosition,
                    isPlaying
                )
            }
        }
    }

    override fun addOrUpdateLingeringOutroOverlay(
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayEntity.isOnScreen = true
        downloaderClient.download(overlayEntity) {
            if (viewHandler.overlayBlueprintIsAttached(it.id)) {
                MLSPlayerView.updateLingeringOutroOverlay(
                    it,
                    animationPosition,
                    isPlaying
                )
            } else {
                MLSPlayerView.addLingeringOutroOverlay(
                    it,
                    animationPosition,
                    isPlaying
                )
            }
        }

    }

    override fun addOrUpdateLingeringMidwayOverlay(overlayEntity: OverlayEntity) {
        overlayEntity.isOnScreen = true
        downloaderClient.download(overlayEntity) {
            if (viewHandler.overlayBlueprintIsAttached(it.id)) {
                MLSPlayerView.updateLingeringMidwayOverlay(it)
            } else {
                MLSPlayerView.addLingeringMidwayOverlay(it)
            }
        }

    }

    override fun removeLingeringOverlay(overlayEntity: OverlayEntity) {
        overlayEntity.isOnScreen = false
        MLSPlayerView.removeLingeringOverlay(overlayEntity)
    }

    override fun setTimelineMarkers(timelineMarkerEntityList: List<TimelineMarkerEntity>) {
        MLSPlayerView.setTimelineMarker(timelineMarkerEntityList)
    }

    override fun clearScreen(idList: List<String>) {
        MLSPlayerView.clearScreen(idList)
    }

    override fun createVariable(setVariable: SetVariableEntity) {
        TODO("Not yet implemented")
    }

    override fun incrementVariable(incrementVariableEntity: IncrementVariableEntity) {
        TODO("Not yet implemented")
    }
}