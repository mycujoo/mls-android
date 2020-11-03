package tv.mycujoo.mls.tv.player

import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.core.IAnnotationListener
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.ViewHandler

class TvAnnotationListener(
    private val tvOverlayContainer: TvOverlayContainer,
    coroutineScope: CoroutineScope,
    countingIdlingResource: CountingIdlingResource,
    private val downloaderClient: DownloaderClient
) : IAnnotationListener {

    val viewHandler: ViewHandler = ViewHandler(coroutineScope, countingIdlingResource)
    private val overlayViewHelper:
            OverlayViewHelper

    init {
        overlayViewHelper =
            OverlayViewHelper(viewHandler, AnimationFactory())
        viewHandler.setOverlayHost(tvOverlayContainer)
    }


    override fun addOverlay(overlayEntity: OverlayEntity) {
        downloaderClient.download(overlayEntity) {
            if (overlayEntity.introTransitionSpec.animationType == AnimationType.NONE) {
                overlayViewHelper.addViewWithNoAnimation(
                    tvOverlayContainer.context,
                    tvOverlayContainer,
                    it
                )
            } else {
                overlayViewHelper.addViewWithAnimation(
                    tvOverlayContainer.context,
                    tvOverlayContainer,
                    it
                )
            }
        }


    }

    override fun removeOverlay(overlayEntity: OverlayEntity) {
        tvOverlayContainer.removeOverlay(overlayEntity)
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