package tv.mycujoo.mls.helper

import org.junit.Assert.assertEquals
import org.junit.Test
import tv.mycujoo.domain.entity.*

class VariableActionHelperTest {
    @Test
    fun `SetVariableAction not reached`() {
        val variable = Variable("\$awayscore", VariableType.LONG, 0)
        val setVariableEntity = SetVariableEntity("id_0", 5000L, variable)

        val act =
            VariableActionHelper.getVariableCurrentAct(3000L, setVariableEntity)

        assertEquals(VariableAct.CLEAR, act)
    }

    @Test
    fun `SetVariableAction reached`() {
        val variable = Variable("\$awayscore", VariableType.LONG, 0)
        val setVariableEntity = SetVariableEntity("id_0", 5000L, variable)

        val act =
            VariableActionHelper.getVariableCurrentAct(4001L, setVariableEntity)

        assertEquals(VariableAct.CREATE_VARIABLE, act)
    }

    @Test
    fun `IncrementVariableAction not reached`() {
        val incrementVariableEntity = IncrementVariableEntity("id_0", 5000L, "\$awayscore", 2L)

        val act =
            VariableActionHelper.getIncrementVariableCurrentAct(3000L, incrementVariableEntity)

        assertEquals(IncrementVariableCurrentAct.DO_NOTHING, act)
    }

    @Test
    fun `IncrementVariableAction reached`() {
        val incrementVariableEntity = IncrementVariableEntity("id_0", 5000L, "\$awayscore", 2L)

        val act =
            VariableActionHelper.getIncrementVariableCurrentAct(4001L, incrementVariableEntity)

        assertEquals(IncrementVariableCurrentAct.INCREMENT, act)
    }
}