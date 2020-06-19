package tv.mycujoo.mls.manager

import android.util.Log
import android.view.View
import tv.mycujoo.mls.helper.TimeRangeHelper.Companion.isInRange
import tv.mycujoo.mls.widgets.mlstimebar.HighlightMarker
import tv.mycujoo.mls.widgets.mlstimebar.HighlightMarkerPosition
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest

class HighlightMarkerManager(
    private val mlsTimeBar: MLSTimeBar,
    private val highlightMarker: HighlightMarker
) {

    private val pointOfInterestList = ArrayList<PointOfInterest>()


    init {

        mlsTimeBar.setHighlightMarkerPositionListener(object : HighlightMarkerPosition {
            override fun onScrubMove(
                position: Long,
                poiPositionOnTimeBarArrayList: ArrayList<Int>
            ) {
                Log.d("HighlightMarkerManager", " onScrubMove pos: $position")
                val highlightIndex = pointOfInterestList.indexOfFirst { pointOfInterest ->
                    isInRange(
                        position,
                        pointOfInterest.offset
                    )
                }

                if (highlightIndex != -1) {
                    if (pointOfInterestList[highlightIndex].title != null && poiPositionOnTimeBarArrayList[highlightIndex] != -1)
                        highlightMarker.setText(pointOfInterestList[highlightIndex].title)
                    highlightMarker.setPosition(poiPositionOnTimeBarArrayList[highlightIndex])

                } else {
                    highlightMarker.visibility = View.INVISIBLE
                    highlightMarker.setText(null)
                }
            }
        })
    }

    fun addTimeLineHighlight(pointOfInterest: PointOfInterest) {
        pointOfInterestList.add(pointOfInterest)
        mlsTimeBar.addTimeLineHighlight(
            pointOfInterest
        )

    }

}
