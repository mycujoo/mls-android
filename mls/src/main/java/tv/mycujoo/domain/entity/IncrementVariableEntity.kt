package tv.mycujoo.domain.entity

data class IncrementVariableEntity(
    val id: String,
    val offset: Long,
    val name: String,
    val amount: Any
)