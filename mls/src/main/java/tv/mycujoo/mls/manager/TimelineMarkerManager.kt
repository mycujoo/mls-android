package tv.mycujoo.mls.manager

import android.util.Log
import android.view.View
import tv.mycujoo.mls.helper.TimeRangeHelper.Companion.isInRange
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarkerPosition
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarkerView

class TimelineMarkerManager(
    private val mlsTimeBar: MLSTimeBar,
    private val timelineMarkerView: TimelineMarkerView
) {

    private val pointOfInterestList = ArrayList<PointOfInterest>()


    init {

        mlsTimeBar.setTimelineMarkerPositionListener(object : TimelineMarkerPosition {
            override fun onScrubMove(
                position: Long,
                videoDuration: Long,
                poiPositionsOnScreen: ArrayList<Int>
            ) {
                Log.d("TimelineMarkerManager", " onScrubMove pos: $position")
                val highlightIndex = pointOfInterestList.indexOfFirst { pointOfInterest ->
                    isInRange(
                        position,
                        videoDuration,
                        pointOfInterest.offset,
                        pointOfInterest.seekOffset
                    )
                }

                if (highlightIndex != -1) {
                    if (pointOfInterestList[highlightIndex].title.isNotEmpty() && poiPositionsOnScreen[highlightIndex] != -1) {

                        timelineMarkerView.setMarkerTexts(pointOfInterestList[highlightIndex].title)
                        timelineMarkerView.setPosition(poiPositionsOnScreen[highlightIndex])
                    }

                } else {
                    timelineMarkerView.visibility = View.INVISIBLE
                    timelineMarkerView.removeMarkerTexts()
                }
            }

            override fun update(position: Long, videoDuration: Long) {

                if (pointOfInterestList.none { isInRange(position, videoDuration, it.offset, it.seekOffset) }) {
                    timelineMarkerView.visibility = View.INVISIBLE
                    timelineMarkerView.removeMarkerTexts()
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
