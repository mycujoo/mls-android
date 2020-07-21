package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.VariableType.*

class ActionVariableHelper {
    companion object {
        fun buildVariablesTillNow(
            currentTime: Long,
            setVariableEntityList: List<SetVariableEntity>,
            incrementVariableEntityList: List<IncrementVariableEntity>
        ): HashMap<String, Any> {

            val updatedVariables = HashMap<String, Any>()

            setVariableEntityList.filter { it.offset <= currentTime }.forEach { setVariableEntity ->

                var initialValue = setVariableEntity.variable.value
                updatedVariables[setVariableEntity.variable.name] = initialValue

                incrementVariableEntityList.filter { it.name == setVariableEntity.variable.name && it.offset < currentTime }
                    .forEach {
                        when (setVariableEntity.variable.type) {
                            DOUBLE -> {
                                if (it.amount is Double) {
                                    initialValue = (initialValue as Double) + it.amount
                                }
                            }
                            LONG -> {
                                if (it.amount is Double) {
                                    initialValue = (initialValue as Long) + it.amount.toLong()
                                }
                            }
                            STRING,
                            UNSPECIFIED -> {
                                // should not happen
                            }
                        }

                        updatedVariables[it.name] = initialValue
                    }


            }

            return updatedVariables

        }
    }
}