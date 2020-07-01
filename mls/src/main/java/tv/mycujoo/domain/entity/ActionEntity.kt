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
    val duration: Long?,
    val animationType: AnimationType,
    val animationDuration: Long?
    )