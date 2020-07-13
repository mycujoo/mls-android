package tv.mycujoo.mls.widgets.mlstimebar

import java.util.ArrayList

interface TimelineMarkerPosition {
    fun onScrubMove(
        position: Long,
        videoDuration: Long,
        poiPositionOnTimeBarArrayList: ArrayList<Int>
    )
}