package tv.mycujoo.mls.manager

import tv.mycujoo.mls.helper.TimeRangeHelper.Companion.isInRange
import tv.mycujoo.mls.widgets.mlstimebar.*

class TimelineMarkerManager(
    private val mlsTimeBar: MLSTimeBar,
    private val timelineMarkerView: TimelineMarkerView
) {

    private val pointOfInterestList = ArrayList<PointOfInterest>()
    private val currentPoiList = ArrayList<PositionedPointOfInterest>()


    init {

        mlsTimeBar.setTimelineMarkerPositionListener(object : TimelineMarkerPosition {
            override fun onScrubMove(
                position: Float,
                positionedPointOfInterestList: ArrayList<PositionedPointOfInterest>
            ) {
                positionedPointOfInterestList.filter { positionedPointOfInterest ->
                    isInRange(
                        position,
                        positionedPointOfInterest.positionOnScreen
                    )
                }.let { inRangePointOfInterestList ->

                    if (inRangePointOfInterestList.isEmpty()) {
                        timelineMarkerView.removeMarkerTexts()

                        currentPoiList.clear()

                        return@let
                    } else {
                        if (isCurrentListChanged(inRangePointOfInterestList)) {
                            currentPoiList.clear()
                            currentPoiList.addAll(inRangePointOfInterestList)

                            timelineMarkerView.setMarkerTexts(
                                currentPoiList.map { it.pointOfInterest.title },
                                position.toInt()
                            )
                        }
                    }

                }
            }

            override fun onScrubStop() {
                currentPoiList.clear()

                timelineMarkerView.removeMarkerTexts()
            }

        })
    }

    private fun isCurrentListChanged(inRangePoiList: List<PositionedPointOfInterest>): Boolean {
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

    fun setTimeLineHighlight(list: List<PointOfInterest>) {
        pointOfInterestList.clear()
        pointOfInterestList.addAll(list)

        mlsTimeBar.clearTimeLineMarker()
        list.forEach {
            mlsTimeBar.addTimeLineHighlight(it)
        }


    }

}
