package tv.mycujoo.mls.manager

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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
}