package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.IncrementVariableCurrentAct
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.VariableAct

class VariableActionHelper {
    companion object {
        fun getVariableCurrentAct(currentTime: Long, variable: SetVariableEntity): VariableAct {
            if (currentTime + 1000L > variable.offset) {
                return VariableAct.CREATE_VARIABLE
            } else {
                return VariableAct.CLEAR
            }
        }

        fun getIncrementVariableCurrentAct(
            currentTime: Long,
            incrementVariableEntity: IncrementVariableEntity
        ): IncrementVariableCurrentAct {
            if (currentTime + 1000L > incrementVariableEntity.offset) {
                return IncrementVariableCurrentAct.INCREMENT
            } else {
                return IncrementVariableCurrentAct.DO_NOTHING
            }

        }
    }
}