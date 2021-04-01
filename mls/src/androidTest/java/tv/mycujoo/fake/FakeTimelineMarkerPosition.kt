package tv.mycujoo.fake

import tv.mycujoo.mcls.widgets.mlstimebar.PositionedPointOfInterest
import tv.mycujoo.mcls.widgets.mlstimebar.TimelineMarkerPosition
import java.util.*

class FakeTimelineMarkerPosition : TimelineMarkerPosition {

    var position: Long = -1L
    var videoDuration: Long = -1L

    override fun onScrubMove(position: Float, positionedPointOfInterestList: ArrayList<PositionedPointOfInterest>) {
        this.position = position.toLong()
    }

    override fun onScrubStop() {
    }

}