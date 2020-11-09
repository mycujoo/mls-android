package tv.mycujoo.mls.helper

import org.junit.Assert.assertEquals
import org.junit.Test
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.domain.entity.VariableAct
import tv.mycujoo.domain.entity.VariableType

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
}