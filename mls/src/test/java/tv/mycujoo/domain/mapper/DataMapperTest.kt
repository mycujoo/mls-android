package tv.mycujoo.domain.mapper

import org.junit.Test
import tv.mycujoo.domain.entity.Variable
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalStdlibApi
class DataMapperTest {
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
}