package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.models.ActionType

data class ActionEntity(
    val id: String,
    val offset: Long,
    val type: ActionType,
    val customId: String?,
    val svgUrl: String?,
    val size: Pair<Float, Float>,
    val duration: Long?
)