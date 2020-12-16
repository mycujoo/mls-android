package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.VariableType.*

class Variable(
    var name: String,
    val type: VariableType,
    var value: Any,
    private val doublePrecision: Int? = null
) {

    fun printValue(): String {
        return when (type) {
            UNSPECIFIED -> {
                ""
            }
            DOUBLE -> {
                if (doublePrecision != null) {
                    String.format("%.${doublePrecision}f", value)
                } else {
                    (value as Double).toString()
                }
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