package tv.mycujoo.domain.entity.models

import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.PositionGuide

data class ParsedOverlayRelatedData(
    val id: String,
    val svgUrl: String,
    val duration: Long,
    val positionGuide: PositionGuide,
    val sizePair: Pair<Float, Float>,
    val introAnimationType: AnimationType,
    val introAnimationDuration: Long,
    val outroAnimationType: AnimationType,
    val outroAnimationDuration: Long,
    val variablePlaceHolders: List<String>
) {
}