package tv.mycujoo.mls.widgets.mlstimebar

import java.util.*

interface TimelineMarkerPosition {
    fun onScrubMove(
        position: Long,
        videoDuration: Long,
        poiPositionOnTimeBarArrayList: ArrayList<Int>
    )

    fun update(position: Long, videoDuration: Long)
}