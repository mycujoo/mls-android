package tv.mycujoo.domain.entity

/**
 * A variable with offset & id
 * @see Variable
 */
data class VariableEntity(
    val id: String,
    val offset: Long,
    val variable: Variable
)