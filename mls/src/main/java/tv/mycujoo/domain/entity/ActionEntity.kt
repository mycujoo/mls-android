package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.models.ActionType
import java.io.InputStream

data class ActionEntity(
    val id: String,
    val offset: Long,
    val type: ActionType,
    val customId: String?,
    val svgUrl: String?,
    val svgInputStream: InputStream?,
    val position : PositionGuide?,
    val size: Pair<Float, Float>,
    var duration: Long?,
    val introAnimationType: AnimationType,
    val introAnimationDuration: Long,
    var outroAnimationType: AnimationType,
    var outroAnimationDuration: Long,
    val variablePlaceHolders: Map<String, String>
)