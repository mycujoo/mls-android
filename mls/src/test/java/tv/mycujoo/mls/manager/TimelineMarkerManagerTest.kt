package tv.mycujoo.mls.manager

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.widgets.mlstimebar.*

class TimelineMarkerManagerTest {


    /**region Subject under test*/
    private lateinit var timelineMarkerManager: TimelineMarkerManager
    /**endregion */

    /**region Fields*/
    private lateinit var timeLineMarkerPositionListener: TimelineMarkerPosition

    @Mock
    lateinit var timeBar: MLSTimeBar

    @Mock
    lateinit var timelineMarkerView: TimelineMarkerView

    /**endregion */

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(timeBar.setTimelineMarkerPositionListener(any())).then {
            timeLineMarkerPositionListener = it.getArgument(0)
            true
        }

        timelineMarkerManager = TimelineMarkerManager(timeBar, timelineMarkerView)
    }

    /**region */
    @Test
    fun `given currentTime equal to left range of timeline marker, should set it to view`() {
        val pointOfInterest = PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType())
        timelineMarkerManager.addTimeLineHighlight(pointOfInterest)
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply { add(PositionedPointOfInterest(490, pointOfInterest)) }
        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(titlesList, position.toInt())
    }

    @Test
    fun `given currentTime equal to right range of timeline marker, should set it to view`() {
        val pointOfInterest = PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType())
        timelineMarkerManager.addTimeLineHighlight(pointOfInterest)
        val titlesList = listOf("Goal")

        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply { add(PositionedPointOfInterest(510, pointOfInterest)) }
        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(titlesList, position.toInt())
    }

    @Test
    fun `given currentTime less than left range of timeline marker, should not set it to view`() {
        val pointOfInterest = PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType())
        timelineMarkerManager.addTimeLineHighlight(pointOfInterest)
        val titlesList = listOf("Goal")

        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply { add(PositionedPointOfInterest(489, pointOfInterest)) }
        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList, position.toInt())
    }

    @Test
    fun `given currentTime greater than right range of timeline marker, should not set it to view`() {
        val pointOfInterest = PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType())
        timelineMarkerManager.addTimeLineHighlight(pointOfInterest)
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply { add(PositionedPointOfInterest(511, pointOfInterest)) }
        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList, position.toInt())
    }

    @Ignore("This should be tested through MLSTimeBar")
    @Test
    fun `given currentTime less than left range + positive seekOffset of timeline marker, should not set it to view`() {
        val pointOfInterest = PointOfInterest(
            10000L,
            3000L,
            listOf("Goal"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest
        )
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply { add(PositionedPointOfInterest(511, pointOfInterest)) }
//        timeLineMarkerPositionListener.onScrubMove(7999L, 60000L, poiPositionsOnScreen)


//        verify(timelineMarkerView, never()).setMarkerTexts(titlesList, position.toInt())
    }

    @Ignore("This should be tested through MLSTimeBar")
    @Test
    fun `given currentTime greater than right range + positive seekOffset of timeline marker, should not set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                3000L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
//        timeLineMarkerPositionListener.onScrubMove(18001L, 60000L, poiPositionsOnScreen)


//        verify(timelineMarkerView, never()).setMarkerTexts(titlesList, position.toInt())
    }

    @Ignore("This should be tested through MLSTimeBar")
    @Test
    fun `given currentTime less than left range + negative seekOffset of timeline marker, should not set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                -3000L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
//        timeLineMarkerPositionListener.onScrubMove(1999L, 60000L, poiPositionsOnScreen)


//        verify(timelineMarkerView, never()).setMarkerTexts(titlesList, position.toInt())
    }

    @Ignore("This should be tested through MLSTimeBar")
    @Test
    fun `given currentTime equal to left range + negative seekOffset of timeline marker, should set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                -3000L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
//        timeLineMarkerPositionListener.onScrubMove(2000L, 60000L, poiPositionsOnScreen)


//        verify(timelineMarkerView).setMarkerTexts(titlesList, position.toInt())
    }

    @Ignore("This should be tested through MLSTimeBar")
    @Test
    fun `given currentTime greater than right range + negative seekOffset of timeline marker, should not set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                -3000L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
//        timeLineMarkerPositionListener.onScrubMove(12001L, 60000L, poiPositionsOnScreen)


//        verify(timelineMarkerView, never()).setMarkerTexts(titlesList, position.toInt())
    }

    @Ignore("This should be tested through MLSTimeBar")
    @Test
    fun `given currentTime equal to right range + negative seekOffset of timeline marker, should set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                -3000L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
//        timeLineMarkerPositionListener.onScrubMove(12000L, 60000L, poiPositionsOnScreen)


//        verify(timelineMarkerView).setMarkerTexts(titlesList, position.toInt())
    }


    @Test
    fun `set timeline marker, should clear previous timeline markers`() {
        timelineMarkerManager.setTimeLineHighlight(emptyList())


        verify(timeBar).clearTimeLineMarker()
    }

    @Test
    fun `set timeline marker, should add to timeBar too`() {
        timelineMarkerManager.setTimeLineHighlight(
            listOf(
                getSamplePointOfInterest(5000L, "Kick-off"),
                getSamplePointOfInterest(25000L, "Foul"),
                getSamplePointOfInterest(50000L, "Goal"),
                getSamplePointOfInterest(75000L, "Foul"),
                getSamplePointOfInterest(100000L, "Half-time")
            )
        )

        verify(
            timeBar, times(5)
        ).addTimeLineHighlight(any())
    }

    @Test
    fun `given timeline marker list, should set all to view`() {
        val pointOfInterest0 = PointOfInterest(
            10000L,
            0L,
            listOf("Goal"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest0
        )
        val pointOfInterest1 = PointOfInterest(
            10000L,
            0L,
            listOf("Assist"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest1
        )
        val markerTitleList = listOf("Goal", "Assist")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply {
                add(PositionedPointOfInterest(500, pointOfInterest0))
                add(PositionedPointOfInterest(500, pointOfInterest1))
            }

        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(markerTitleList, position.toInt())
    }

    @Test
    fun `given a timeline marker list multiple time, should not set it to view more than once`() {
        val pointOfInterest0 = PointOfInterest(
            10000L,
            0L,
            listOf("Goal"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest0
        )
        val pointOfInterest1 = PointOfInterest(
            10000L,
            0L,
            listOf("Assist"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest1
        )
        val markerTitleList = listOf("Goal", "Assist")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply {
                add(PositionedPointOfInterest(500, pointOfInterest0))
                add(PositionedPointOfInterest(500, pointOfInterest1))
            }

        timeLineMarkerPositionListener.onScrubMove(500F, poiPositionsOnScreen)
        timeLineMarkerPositionListener.onScrubMove(501F, poiPositionsOnScreen)


        verify(timelineMarkerView, times(1)).setMarkerTexts(any(), any())
    }

    @Test
    fun `given multiple different timeline marker list for different time, should set them to view`() {
        val pointOfInterest0 = PointOfInterest(
            10000L,
            0L,
            listOf("Goal"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest0
        )
        val pointOfInterest1 = PointOfInterest(
            10000L,
            0L,
            listOf("Assist"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest1
        )
        val firstMarkerTitleList = listOf("Goal", "Assist")

        val pointOfInterest2 = PointOfInterest(
            50000L,
            0L,
            listOf("Foul"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest2
        )
        val pointOfInterest3 = PointOfInterest(
            50000L,
            0L,
            listOf("Red card"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest3
        )
        val secondMarkerTitleList = listOf("Foul", "Red card")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply {
                add(PositionedPointOfInterest(500, pointOfInterest0))
                add(PositionedPointOfInterest(500, pointOfInterest1))
                add(PositionedPointOfInterest(600, pointOfInterest2))
                add(PositionedPointOfInterest(600, pointOfInterest3))
            }

        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)
        val position1 = 600F
        timeLineMarkerPositionListener.onScrubMove(position1, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(firstMarkerTitleList, position.toInt())
        verify(timelineMarkerView).setMarkerTexts(secondMarkerTitleList, position1.toInt())
    }

    @Test
    fun `given multiple different timeline marker list with common element, should set them to view`() {
        val pointOfInterest0 = PointOfInterest(
            10000L,
            0L,
            listOf("Goal"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest0
        )
        val pointOfInterest1 = PointOfInterest(
            10000L,
            0L,
            listOf("Assist"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest1
        )
        val pointOfInterest2 = PointOfInterest(
            15000L,
            0L,
            listOf("Foul"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest2
        )
        val firstMarkerTitleListPlusCommonElement = listOf("Goal", "Assist", "Foul")

        val pointOfInterest3 = PointOfInterest(
            20000L,
            0L,
            listOf("Red card"),
            PointOfInterestType()
        )
        timelineMarkerManager.addTimeLineHighlight(
            pointOfInterest3
        )
        val secondMarkerTitleListPlusCommonElement = listOf("Foul", "Red card")


        val poiPositionsOnScreen =
            ArrayList<PositionedPointOfInterest>().apply {
                add(PositionedPointOfInterest(500, pointOfInterest0))
                add(PositionedPointOfInterest(500, pointOfInterest1))
                add(PositionedPointOfInterest(510, pointOfInterest2))
                add(PositionedPointOfInterest(520, pointOfInterest3))
            }

        val position = 500F
        timeLineMarkerPositionListener.onScrubMove(position, poiPositionsOnScreen)
        val position1 = 520F
        timeLineMarkerPositionListener.onScrubMove(position1, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(firstMarkerTitleListPlusCommonElement, position.toInt())
        verify(timelineMarkerView).setMarkerTexts(secondMarkerTitleListPlusCommonElement, position1.toInt())
    }

    companion object {
        fun getSamplePointOfInterest(offset: Long, title: String): PointOfInterest {
            return PointOfInterest(offset, 10000L, listOf(title), PointOfInterestType())
        }
    }


}