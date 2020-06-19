package tv.mycujoo.mls.widgets.mlstimebar

import java.util.ArrayList

interface HighlightMarkerPosition {
    fun onScrubMove(
        position: Long,
        poiPositionOnTimeBarArrayList: ArrayList<Int>
    )
}