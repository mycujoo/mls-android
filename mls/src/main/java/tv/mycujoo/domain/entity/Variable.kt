package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.VariableType.*

class Variable(var name: String, val type: VariableType, var value: Any) {

    fun printValue(): String {
        return when (type) {
            UNSPECIFIED -> {
                ""
            }
            DOUBLE -> {
                (value as Double).toString()
            }
            LONG -> {
                (value as Long).toString()
            }
            STRING -> {
                (value as String)
            }
        }
    }
}