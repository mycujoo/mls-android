package tv.mycujoo.domain.mapper

import org.junit.Test
import tv.mycujoo.domain.entity.ActionSourceData
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalStdlibApi
class TimelineMarkerMapperTest {
    @Test
    fun `given valid action, should map to TimelineMarker`() {
        val seekOffset: Double = 5000.toDouble()
        val title = "Goal"
        val color = "#334455"


        val actionSourceData =
            ActionSourceData("id_01", "show_timeline_marker", 5000L, -1L, buildMap {
                put("seek_offset", seekOffset)
                put("label", title)
                put("color", color)
            })


        val timelineMarker = TimelineMarkerMapper.mapToTimelineMarker(actionSourceData)


        assertNotNull(timelineMarker)
        assertEquals(seekOffset.toLong(), timelineMarker.seekOffset)
        assertEquals(title, timelineMarker.label)
        assertEquals(color, timelineMarker.color)
    }

    @Test
    fun `given invalid actionSourceData, should return null`() {
        val actionSourceDataWithNullId =
            ActionSourceData(null, "show_timeline_marker", 5000L, -1L, buildMap {})
        val actionSourceDataWitWrongType =
            ActionSourceData(null, "show_overlay", 5000L, -1L, buildMap {})


        val timelineMarkerFromNullId =
            TimelineMarkerMapper.mapToTimelineMarker(actionSourceDataWithNullId)
        val timelineMarkerFromWrongType =
            TimelineMarkerMapper.mapToTimelineMarker(actionSourceDataWitWrongType)


        assertNull(timelineMarkerFromNullId)
        assertNull(timelineMarkerFromWrongType)
    }
}