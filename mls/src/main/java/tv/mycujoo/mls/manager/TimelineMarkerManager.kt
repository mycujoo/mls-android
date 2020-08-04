package tv.mycujoo.mls.manager

import android.util.Log
import android.view.View
import tv.mycujoo.mls.helper.TimeRangeHelper.Companion.isInRange
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarker
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarkerPosition

class TimelineMarkerManager(
    private val mlsTimeBar: MLSTimeBar,
    private val timelineMarker: TimelineMarker
) {

    private val pointOfInterestList = ArrayList<PointOfInterest>()


    init {

        mlsTimeBar.setHighlightMarkerPositionListener(object : TimelineMarkerPosition {
            override fun onScrubMove(
                position: Long,
                videoDuration: Long,
                poiPositionOnTimeBarArrayList: ArrayList<Int>
            ) {
                Log.d("TimelineMarkerManager", " onScrubMove pos: $position")
                val highlightIndex = pointOfInterestList.indexOfFirst { pointOfInterest ->
                    isInRange(
                        position,
                        videoDuration,
                        pointOfInterest.offset
                    )
                }

                if (highlightIndex != -1) {
                    if (pointOfInterestList[highlightIndex].title.isNotEmpty() && poiPositionOnTimeBarArrayList[highlightIndex] != -1) {

                        timelineMarker.addHighlightTexts(pointOfInterestList[highlightIndex].title)
                        timelineMarker.setPosition(poiPositionOnTimeBarArrayList[highlightIndex])
                    }

                } else {
                    timelineMarker.visibility = View.INVISIBLE
                    timelineMarker.removeTexts()
                }
            }

            override fun update(position: Long, videoDuration: Long) {

                if (pointOfInterestList.none { isInRange(position, videoDuration, it.offset) }) {
                    timelineMarker.visibility = View.INVISIBLE
                    timelineMarker.removeTexts()
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
