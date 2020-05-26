package tv.mycujoo.mls.cordinator

import android.os.Handler
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mls.core.AnnotationBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.AnnotationPublisher
import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.entity.OverLayAction
import tv.mycujoo.mls.entity.TimeLineAction
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.HighlightAdapter
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.TimeLineSeekBar

class Coordinator(
    private val api: Api,
    private val publisher: AnnotationPublisher
) {

    var timeLineSeekBar: TimeLineSeekBar? = null
    internal lateinit var playerViewWrapper: PlayerViewWrapper

    fun initialize(
        exoPlayer: SimpleExoPlayer,
        handler: Handler,
        highlightAdapter: HighlightAdapter?
    ) {
        val listener = object : AnnotationListener {
            override fun onNewAnnotationAvailable(annotationSourceData: AnnotationSourceData) {
                when (annotationSourceData.action) {
                    is OverLayAction -> {
                        playerViewWrapper.showOverLay(annotationSourceData.action)
                    }
                    is HighlightAction -> {

                        highlightAdapter?.addHighlight(annotationSourceData.action)
                    }
                    is TimeLineAction -> {

                    }
                    else -> {
                    }
                }
            }


            override fun onNewActionWrapperAvailable(actionWrapper: ActionWrapper) {
                when (actionWrapper.action) {
                    is ShowAnnouncementOverlayAction -> {
                        playerViewWrapper.showAnnouncementOverLay(actionWrapper.action as ShowAnnouncementOverlayAction)
                    }
                    else -> {
                    }
                }
            }
        }
        publisher.setAnnotationListener(listener)


        val annotationBuilder = AnnotationBuilderImpl(publisher)
        annotationBuilder.buildPendings()

        annotationBuilder.addPendingAnnotations(api.getAnnotations())
        annotationBuilder.addPendingActions(api.getActions())

        val runnable = object : Runnable {
            override fun run() {
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition)
                annotationBuilder.buildPendings()
                handler.postDelayed(this, 1000L)
            }
        }

        handler.postDelayed(runnable, 1000L)
    }

    fun onPlayVideo() {
        val timeLineList = api.getTimeLineMarkers()

        val longArray = LongArray(timeLineList.size)
        val booleanArray = BooleanArray(timeLineList.size)
        timeLineList.forEachIndexed() { index, timeLineItem ->
            longArray[index] = timeLineItem.streamOffset
            booleanArray[index] = false
        }
        playerViewWrapper.addMarker(longArray, booleanArray)

    }
}