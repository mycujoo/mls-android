package tv.mycujoo.mcls.widgets.mlstimebar

import java.util.*

interface TimelineMarkerPosition {
    fun onScrubMove(
        position: Float,
        positionedPointOfInterestList: ArrayList<PositionedPointOfInterest>
    )

    fun onScrubStop()
}