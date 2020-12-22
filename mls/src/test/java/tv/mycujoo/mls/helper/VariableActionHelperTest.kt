package tv.mycujoo.mls.helper

import org.junit.Assert.assertEquals
import org.junit.Test
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.IncrementVariableCurrentAct
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.domain.entity.VariableAct

class VariableActionHelperTest {
    @Test
    fun `SetVariableAction not reached`() {
        val variable = Variable.LongVariable("\$awayscore", 0L)
        val createVariableAction = Action.CreateVariableAction("id_0", 5000L, -1L, variable)

        val act =
            VariableActionHelper.getVariableCurrentAct(3000L, createVariableAction)

        assertEquals(VariableAct.CLEAR, act)
    }

    @Test
    fun `SetVariableAction reached`() {
        val variable = Variable.LongVariable("\$awayscore", 0L)
        val createVariableAction = Action.CreateVariableAction("id_0", 5000L, -1L, variable)

        val act =
            VariableActionHelper.getVariableCurrentAct(4001L, createVariableAction)

        assertEquals(VariableAct.CREATE_VARIABLE, act)
    }

    @Test
    fun `IncrementVariableAction not reached`() {
        val incrementVariableAction =
            Action.IncrementVariableAction("id_0", 5000L, -1L, "\$awayscore", 2.toDouble())

        val act =
            VariableActionHelper.getIncrementVariableCurrentAct(3000L, incrementVariableAction)

        assertEquals(IncrementVariableCurrentAct.DO_NOTHING, act)
    }

    @Test
    fun `IncrementVariableAction reached`() {
        val incrementVariableAction =
            Action.IncrementVariableAction("id_0", 5000L, -1L, "\$awayscore", 2.toDouble())

        val act =
            VariableActionHelper.getIncrementVariableCurrentAct(4001L, incrementVariableAction)

        assertEquals(IncrementVariableCurrentAct.INCREMENT, act)
    }
}