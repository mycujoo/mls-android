package tv.mycujoo.domain.entity

data class SetVariableEntity(
    val id: String,
    val offset: Long,
    val variable: Variable
)