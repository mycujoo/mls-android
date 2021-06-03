package tv.mycujoo.domain.entity.models

import tv.mycujoo.domain.entity.VariableType

/**
 * Extracted data needed to create a SetVariable
 */
data class ExtractedSetVariableData(
    val name: String? = null,
    val variableType: VariableType = VariableType.UNSPECIFIED,
    var variableValue: Any? = null,
    var variableDoublePrecision: Int? = null
)
