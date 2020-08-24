package tv.mycujoo.mls.widgets.mlstimebar

import java.util.*

interface TimelineMarkerPosition {
    fun onScrubMove(
        position: Long,
        videoDuration: Long,
        poiPositionsOnScreen: ArrayList<Int>
    )
    fun onScrubStop()

    fun update(position: Long, videoDuration: Long)
}