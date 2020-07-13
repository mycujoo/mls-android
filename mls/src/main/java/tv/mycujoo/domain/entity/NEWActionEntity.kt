package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.model.MutablePair

data class NEWActionEntity(
    val id: String,
    val offset: Long,
    val type: ActionType,
    val customId: String,
    val svgUrl: String,
    val positionGuide: PositionGuide, // either value or null for each
    val sizePair: MutablePair<Float, Float>, // either value or -1 for each
    val duration: Long,
    val introAnimationType: AnimationType,
    val introAnimationDuration: Long,
    val outroAnimationType: AnimationType,
    val outroAnimationDuration: Long,
    val label : String?,
    val color : String?
)