package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.IncrementVariableCurrentAct
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.VariableAct
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS

class VariableActionHelper {
    companion object {
        fun getVariableCurrentAct(currentTime: Long, variable: SetVariableEntity): VariableAct {
            if (currentTime + ONE_SECOND_IN_MS > variable.offset) {
                return VariableAct.CREATE_VARIABLE
            } else {
                return VariableAct.CLEAR
            }
        }

        fun getIncrementVariableCurrentAct(
            currentTime: Long,
            incrementVariableEntity: IncrementVariableEntity
        ): IncrementVariableCurrentAct {
            if (currentTime + ONE_SECOND_IN_MS > incrementVariableEntity.offset) {
                return IncrementVariableCurrentAct.INCREMENT
            } else {
                return IncrementVariableCurrentAct.DO_NOTHING
            }

        }
    }
}