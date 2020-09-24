package tv.mycujoo.mls.manager

import tv.mycujoo.mls.helper.TimeRangeHelper
import tv.mycujoo.mls.tv.widgets.MLSSeekBar
import tv.mycujoo.mls.widgets.mlstimebar.PointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.PositionedPointOfInterest
import tv.mycujoo.mls.widgets.mlstimebar.TimelineMarkerWidget

class TvTimelineMarkerManager(
    private val seekBar: MLSSeekBar,
    private val timelineMarkerView: TimelineMarkerWidget
) {

    private val pointOfInterestList = ArrayList<PointOfInterest>()
    private val currentPoiList = ArrayList<PositionedPointOfInterest>()

    fun addTimeLineHighlight(pointOfInterest: PointOfInterest) {
        pointOfInterestList.add(pointOfInterest)
        seekBar.addTimeLineHighlight(
            pointOfInterest
        )

    }

    init {
        seekBar.setSeekbarListener(object : SeekBarListener {

            override fun onSeekTo(
                position: Int,
                positionedPointOfInterestList: ArrayList<PositionedPointOfInterest>
            ) {

                positionedPointOfInterestList.filter { positionedPointOfInterest ->
                    TimeRangeHelper.isInRangeTV(
                        position.toFloat(),
                        positionedPointOfInterest.positionOnScreen
                    )
                }.let { inRangePointOfInterestList ->

                    if (inRangePointOfInterestList.isEmpty()) {
                        timelineMarkerView.removeMarkerTexts()
                        currentPoiList.clear()

                        return@let
                    }

                    if (isCurrentListChanged(inRangePointOfInterestList)) {
                        currentPoiList.clear()
                        currentPoiList.addAll(inRangePointOfInterestList)

                        timelineMarkerView.setMarkerTexts(
                            currentPoiList.map { it.pointOfInterest.title },
                            position
                        )
                    }


                }
            }

        })
    }

    private fun isCurrentListChanged(inRangePoiList: List<PositionedPointOfInterest>): Boolean {
        if (currentPoiList.size != inRangePoiList.size) {
            return true
        }

        return currentPoiList.containsAll(inRangePoiList).not() || inRangePoiList.containsAll(
            currentPoiList
        ).not()
    }


}