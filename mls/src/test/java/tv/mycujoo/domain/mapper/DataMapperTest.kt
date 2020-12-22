package tv.mycujoo.domain.mapper

import org.junit.Test
import tv.mycujoo.domain.entity.Variable
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalStdlibApi
class DataMapperTest {

    /**region CreateVariable functions*/
    @Test
    fun `map to DoubleVariable`() {

        val data = buildMap<String, Any> {
            put("name", "var1")
            put("value", 5000.toDouble())
            put("type", "double")
            put("double_precision", 2)
        }


        val variable = DataMapper.mapToVariable(data)


        assertTrue { variable is Variable.DoubleVariable }
        assertEquals(data["name"], variable.name)
        assertEquals("5000.00", variable.printValue())
    }

    @Test
    fun `map to LongVariable`() {

        val data = buildMap<String, Any> {
            put("name", "var1")
            put("value", 5000.toLong())
            put("type", "long")
        }


        val variable = DataMapper.mapToVariable(data)


        assertTrue { variable is Variable.LongVariable }
        assertEquals(data["name"], variable.name)
        assertEquals(data["value"].toString(), variable.printValue())
    }

    @Test
    fun `map to StringVariable`() {

        val data = buildMap<String, Any> {
            put("name", "var1")
            put("value", "STR")
            put("type", "string")
        }


        val variable = DataMapper.mapToVariable(data)


        assertTrue { variable is Variable.StringVariable }
        assertEquals(data["name"], variable.name)
        assertEquals(data["value"], variable.printValue())
    }
    /**endregion */

    /**region IncrementVariable functions*/
    @Test
    fun `extract IncrementVariable data with no data should return null`() {
        val extractedIncrementVariableData = DataMapper.extractIncrementVariableData(null)


        assertNull(extractedIncrementVariableData)
    }

    @Test
    fun `extract IncrementVariable data with no name & amount should return null`() {
        val data = buildMap<String, Any> {
            put(UNRELATED, UNRELATED)
        }


        val extractedIncrementVariableData = DataMapper.extractIncrementVariableData(data)


        assertNull(extractedIncrementVariableData)
    }

    @Test
    fun `extract IncrementVariable data`() {
        val data = buildMap<String, Any> {
            put("name", "var1")
            put("amount", 5000.toDouble())
        }


        val extractedIncrementVariableData = DataMapper.extractIncrementVariableData(data)


        assertEquals(data["name"], extractedIncrementVariableData!!.name)
        assertEquals(data["amount"], extractedIncrementVariableData.amount)
    }

    /**endregion */

    /**region MarkTimeline functions*/
    @Test
    fun `extract MarkTimeline data with seek_offset`() {
        val data = buildMap<String, Any> {
            put("seek_offset", 10.toLong())
            put("label", "#cccccc")
            put("color", "Goal")
        }


        val extractedMarkTimelineData = DataMapper.extractMarkTimelineData(data)


        assertEquals(data["seek_offset"], extractedMarkTimelineData!!.seekOffset)
        assertEquals(data["label"], extractedMarkTimelineData.label)
        assertEquals(data["color"], extractedMarkTimelineData.color)
    }

    @Test
    fun `extract MarkTimeline data without seek_offset`() {
        val data = buildMap<String, Any> {
            put("label", "#cccccc")
            put("color", "Goal")
        }


        val extractedMarkTimelineData = DataMapper.extractMarkTimelineData(data)


        assertEquals(0L, extractedMarkTimelineData!!.seekOffset)
        assertEquals(data["label"], extractedMarkTimelineData.label)
        assertEquals(data["color"], extractedMarkTimelineData.color)
    }
    /**endregion */

    /**region DeleteAction functions*/
    @Test
    fun `extract DeleteAction data with action_id`() {
        val data = buildMap<String, Any> {
            put("action_id", RANDOM_STRING)
        }


        val extractedMarkTimelineData = DataMapper.extractDeleteActionData(data)


        assertEquals(data["action_id"], extractedMarkTimelineData)
    }

    @Test
    fun `extract DeleteAction data without action_id`() {
        val data = buildMap<String, Any> {
            put(UNRELATED, RANDOM_STRING)
        }


        val extractedMarkTimelineData = DataMapper.extractDeleteActionData(data)


        assertEquals(null, extractedMarkTimelineData)
    }

    /**endregion */

    companion object {
        const val UNRELATED = "unrelated"
        const val RANDOM_STRING = "random_string"
    }
}