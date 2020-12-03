package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.domain.entity.VariableType.*

class ActionVariableHelper {
    companion object {
        fun incrementVariable(
            variable: Variable,
            incrementVariable: IncrementVariableEntity
        ) {
            var initialValue = variable.value
            when (variable.type) {

                UNSPECIFIED -> {
                    // do nothing
                }
                DOUBLE -> {
                    if (incrementVariable.amount is Double) {
                        initialValue =
                            (initialValue as Double) + incrementVariable.amount
                    }
                    if (incrementVariable.amount is Long) {
                        initialValue =
                            (initialValue as Double) + incrementVariable.amount.toDouble()
                    }
                }
                LONG -> {
                    if (incrementVariable.amount is Double) {
                        initialValue =
                            (initialValue as Long) + incrementVariable.amount.toLong()
                    }
                    if (incrementVariable.amount is Long) {
                        initialValue =
                            (initialValue as Long) + incrementVariable.amount
                    }
                }
                STRING -> {

                }
            }
            variable.value = initialValue
        }
    }
}