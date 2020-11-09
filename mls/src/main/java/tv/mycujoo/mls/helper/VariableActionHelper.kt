package tv.mycujoo.mls.helper

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
    }
}