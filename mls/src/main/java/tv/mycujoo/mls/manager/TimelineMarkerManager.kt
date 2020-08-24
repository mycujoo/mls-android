package tv.mycujoo.mls.manager

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
    private val currentPoiList = ArrayList<PointOfInterest>()


    init {

        mlsTimeBar.setTimelineMarkerPositionListener(object : TimelineMarkerPosition {
            private var scrubbing = false

            override fun onScrubMove(
                position: Long,
                videoDuration: Long,
                poiPositionsOnScreen: ArrayList<Int>
            ) {
                scrubbing = true

                pointOfInterestList.filterIndexed { index, pointOfInterest ->
                    isInRange(
                        position,
                        videoDuration,
                        pointOfInterest.offset,
                        pointOfInterest.seekOffset
                    )
                }.let { inRangePointOfInterestList ->

                    if (inRangePointOfInterestList.isEmpty()) {
                        timelineMarkerView.visibility = View.INVISIBLE
                        timelineMarkerView.removeMarkerTexts()

                        currentPoiList.clear()

                        return@let
                    }

                    if (isCurrentListChanged(inRangePointOfInterestList)) {
                        currentPoiList.clear()
                        currentPoiList.addAll(inRangePointOfInterestList)

                        timelineMarkerView.setMarkerTexts(inRangePointOfInterestList.flatMap { it.title })
                    }


                    val markerIndex = pointOfInterestList.indexOfFirst { pointOfInterest ->
                        isInRange(
                            position,
                            videoDuration,
                            pointOfInterest.offset,
                            pointOfInterest.seekOffset
                        )
                    }
                    if (markerIndex != -1) {
                        timelineMarkerView.setPosition(poiPositionsOnScreen[markerIndex])

                    }
                }
            }

            override fun onScrubStop() {
                scrubbing = false
            }

            override fun update(position: Long, videoDuration: Long) {
                if (scrubbing) {
                    return
                }
                if (pointOfInterestList.none { isInRange(position, videoDuration, it.offset, it.seekOffset) }) {

                    currentPoiList.clear()

                    timelineMarkerView.visibility = View.INVISIBLE
                    timelineMarkerView.removeMarkerTexts()
                }

            }
        })
    }

    private fun isCurrentListChanged(inRangePoiList: List<PointOfInterest>): Boolean {
        if (currentPoiList.size != inRangePoiList.size) {
            return true
        }

        return currentPoiList.containsAll(inRangePoiList).not() || inRangePoiList.containsAll(currentPoiList).not()
    }

    fun addTimeLineHighlight(pointOfInterest: PointOfInterest) {
        pointOfInterestList.add(pointOfInterest)
        mlsTimeBar.addTimeLineHighlight(
            pointOfInterest
        )

    }

}
