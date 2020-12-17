package tv.mycujoo.domain.entity.models

import tv.mycujoo.domain.entity.VariableType

data class ExtractedSetVariableData(
    val name: String? = null,
    val variableType: VariableType = VariableType.UNSPECIFIED,
    var variableValue: Any? = null,
    var variableDoublePrecision: Int? = null
)
