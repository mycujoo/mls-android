package tv.mycujoo.domain.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class VariableTest {

    @Test
    fun `print value of Double variable`() {
        val variable = Variable.DoubleVariable(SAMPLE_NAME, 45.toDouble())

        assertEquals("45.0", variable.printValue())
    }

    @Test
    fun `print value of Double variable with double_precision`() {
        val variableDoublePrecision1 = Variable.DoubleVariable(SAMPLE_NAME, 45.toDouble(), 1)
        val variableDoublePrecision2 = Variable.DoubleVariable(SAMPLE_NAME, 45.toDouble(), 2)
        val variableDoublePrecision3 = Variable.DoubleVariable(SAMPLE_NAME, 45.toDouble(), 3)

        assertEquals("45.0", variableDoublePrecision1.printValue())
        assertEquals("45.00", variableDoublePrecision2.printValue())
        assertEquals("45.000", variableDoublePrecision3.printValue())
    }

    @Test
    fun `print value of Long variable`() {
        val variable = Variable.LongVariable(SAMPLE_NAME, 1000L)

        assertEquals("1000", variable.printValue())
    }


    @Test
    fun `print value of String variable`() {
        val variable = Variable.StringVariable(SAMPLE_NAME, "STR")

        assertEquals("STR", variable.printValue())
    }

    @Test
    fun `print value of Unspecified variable`() {
        val variable = Variable.InvalidVariable(SAMPLE_NAME)

        assertEquals("", variable.printValue())
    }


    companion object {
        const val SAMPLE_NAME = "sample_name"
    }
}