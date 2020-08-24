package tv.mycujoo.fake

import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarkerPosition
import java.util.*

class FakeTimelineMarkerPosition : TimelineMarkerPosition {

    var position: Long = -1L
    var videoDuration: Long = -1L

    override fun onScrubMove(position: Long, videoDuration: Long, poiPositionsOnScreen: ArrayList<Int>) {
        this.position = position
        this.videoDuration = videoDuration
    }

    override fun onScrubStop() {
    }

    override fun update(position: Long, videoDuration: Long) {
        this.position = position
        this.videoDuration = videoDuration
    }
}