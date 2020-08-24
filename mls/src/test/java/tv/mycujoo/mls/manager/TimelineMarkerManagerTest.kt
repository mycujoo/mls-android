package tv.mycujoo.mls.manager

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
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
        timelineMarkerManager.addTimeLineHighlight(PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType()))
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
        timeLineMarkerPositionListener.onScrubMove(5000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(titlesList)
    }

    @Test
    fun `given currentTime equal to right range of timeline marker, should set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType()))
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
        timeLineMarkerPositionListener.onScrubMove(15000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(titlesList)
    }

    @Test
    fun `given currentTime less than left range of timeline marker, should not set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType()))
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
        timeLineMarkerPositionListener.onScrubMove(4999, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList)
    }

    @Test
    fun `given currentTime greater than right range of timeline marker, should not set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(PointOfInterest(10000L, 0L, listOf("Goal"), PointOfInterestType()))
        val titlesList = listOf("Goal")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
        timeLineMarkerPositionListener.onScrubMove(15001L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList)
    }

    @Test
    fun `given currentTime less than left range + positive seekOffset of timeline marker, should not set it to view`() {
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
        timeLineMarkerPositionListener.onScrubMove(7999L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList)
    }

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
        timeLineMarkerPositionListener.onScrubMove(18001L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList)
    }

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
        timeLineMarkerPositionListener.onScrubMove(1999L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList)
    }

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
        timeLineMarkerPositionListener.onScrubMove(2000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(titlesList)
    }

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
        timeLineMarkerPositionListener.onScrubMove(12001L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, never()).setMarkerTexts(titlesList)
    }

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
        timeLineMarkerPositionListener.onScrubMove(12000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(titlesList)
    }


    @Test
    fun `given timeline marker list, should set all to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Assist"),
                PointOfInterestType()
            )
        )
        val markerTitleList = listOf("Goal", "Assist")


        val poiPositionsOnScreen = ArrayList<Int>().apply { add(333) }
        timeLineMarkerPositionListener.onScrubMove(12000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(markerTitleList)
    }

    @Test
    fun `given a timeline marker list multiple time, should not set it to view more than once`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Assist"),
                PointOfInterestType()
            )
        )
        val markerTitleList = listOf("Goal", "Assist")


        val poiPositionsOnScreen = ArrayList<Int>().apply { addAll(listOf(333, 333, 666, 666)) }
        timeLineMarkerPositionListener.onScrubMove(12000L, 60000L, poiPositionsOnScreen)
        timeLineMarkerPositionListener.onScrubMove(12001L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView, times(1)).setMarkerTexts(markerTitleList)
    }

    @Test
    fun `given multiple different timeline marker list, should set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Assist"),
                PointOfInterestType()
            )
        )
        val firstMarkerTitleList = listOf("Goal", "Assist")
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                50000L,
                0L,
                listOf("Foul"),
                PointOfInterestType()
            )
        )
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                50000L,
                0L,
                listOf("Red card"),
                PointOfInterestType()
            )
        )
        val secondMarkerTitleList = listOf("Foul", "Red card")


        val poiPositionsOnScreen = ArrayList<Int>().apply { addAll(listOf(333, 333, 666, 666)) }
        timeLineMarkerPositionListener.onScrubMove(12000L, 60000L, poiPositionsOnScreen)
        timeLineMarkerPositionListener.onScrubMove(48000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(firstMarkerTitleList)
        verify(timelineMarkerView).setMarkerTexts(secondMarkerTitleList)
    }

    @Test
    fun `given multiple different timeline marker list with common element, should set it to view`() {
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Goal"),
                PointOfInterestType()
            )
        )
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                10000L,
                0L,
                listOf("Assist"),
                PointOfInterestType()
            )
        )
        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                15000L,
                0L,
                listOf("Foul"),
                PointOfInterestType()
            )
        )
        val firstMarkerTitleListPlusCommonElement = listOf("Goal", "Assist", "Foul")

        timelineMarkerManager.addTimeLineHighlight(
            PointOfInterest(
                20000L,
                0L,
                listOf("Red card"),
                PointOfInterestType()
            )
        )
        val secondMarkerTitleListPlusCommonElement = listOf("Foul", "Red card")


        val poiPositionsOnScreen = ArrayList<Int>().apply { addAll(listOf(333, 333, 666, 666)) }
        timeLineMarkerPositionListener.onScrubMove(12000L, 60000L, poiPositionsOnScreen)
        timeLineMarkerPositionListener.onScrubMove(18000L, 60000L, poiPositionsOnScreen)


        verify(timelineMarkerView).setMarkerTexts(firstMarkerTitleListPlusCommonElement)
        verify(timelineMarkerView).setMarkerTexts(secondMarkerTitleListPlusCommonElement)
    }


}