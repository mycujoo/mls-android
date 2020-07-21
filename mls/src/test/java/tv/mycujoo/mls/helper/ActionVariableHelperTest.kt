package tv.mycujoo.mls.helper

import org.junit.Assert.assertTrue
import org.junit.Test
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.domain.entity.VariableType
import kotlin.test.assertEquals

class ActionVariableHelperTest {

    @Test
    fun `given no SetVariable, should return empty`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(3000L, emptyList(), emptyList())

        assertTrue(variablesTillNow.isEmpty())
    }

    private val sampleName = "name_0"

    @Test
    fun `given Long variable with initial value, should return that`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.LONG, 0L)),
                emptyList()
            )

        assertEquals(0L, variablesTillNow[sampleName])
    }

    @Test
    fun `given Double variable with Initial value, should return that`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.DOUBLE, 0.0)),
                emptyList()
            )

        assertEquals(0.0, variablesTillNow[sampleName])
    }

    @Test
    fun `given String variable with Initial value, should return that`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.STRING, "str")),
                emptyList()
            )

        assertEquals("str", variablesTillNow[sampleName])
    }

    @Test
    fun `given Long variable with initial value, and an increment, should return total`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.LONG, 0L)),
                listOf(sampleIncrementVariableEntity(sampleName, 1L))
            )

        assertEquals(1L, variablesTillNow[sampleName])
    }

    @Test
    fun `given Long variable with initial value, and multiple increments, should return total`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.LONG, 0L)),
                listOf(
                    sampleIncrementVariableEntity(sampleName, 1L),
                    sampleIncrementVariableEntity(sampleName, 1L)
                )
            )

        assertEquals(2L, variablesTillNow[sampleName])
    }

    @Test
    fun `given Long variable with initial value, and multiple increments including negative increments, should return total`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.LONG, 0L)),
                listOf(
                    sampleIncrementVariableEntity(sampleName, 1L),
                    sampleIncrementVariableEntity(sampleName, 5L),
                    sampleIncrementVariableEntity(sampleName, -1L)
                )
            )

        assertEquals(5L, variablesTillNow[sampleName])
    }

    @Test
    fun `given Double variable with initial value, and an increment, should return total`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.DOUBLE, 0.0)),
                listOf(sampleIncrementVariableEntity(sampleName, 1.0))
            )

        assertEquals(1.0, variablesTillNow[sampleName])
    }

    @Test
    fun `given Double variable with initial value, and multiple increments, should return total`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.DOUBLE, 0.0)),
                listOf(
                    sampleIncrementVariableEntity(sampleName, 1.0),
                    sampleIncrementVariableEntity(sampleName, 1.0)
                )
            )

        assertEquals(2.0, variablesTillNow[sampleName])
    }

    @Test
    fun `given Double variable with initial value, and multiple increments including negative increments, should return total`() {
        val variablesTillNow =
            ActionVariableHelper.buildVariablesTillNow(
                3000L,
                listOf(sampleSetVariableEntity(sampleName, VariableType.DOUBLE, 0.0)),
                listOf(
                    sampleIncrementVariableEntity(sampleName, 1.0),
                    sampleIncrementVariableEntity(sampleName, 5.0),
                    sampleIncrementVariableEntity(sampleName, -1.0)
                )
            )

        assertEquals(5.0, variablesTillNow[sampleName])
    }


    private fun sampleSetVariableEntity(
        name: String,
        variableType: VariableType,
        variableValue: Any
    ): SetVariableEntity {
        val variable = Variable(name, variableType, variableValue)
        return SetVariableEntity("1001", 0L, variable)
    }

    private fun sampleIncrementVariableEntity(name: String, amount: Any): IncrementVariableEntity {
        return IncrementVariableEntity("1001", 1L, name, amount)
    }
}