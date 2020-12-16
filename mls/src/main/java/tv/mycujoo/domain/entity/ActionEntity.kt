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
    val position: PositionGuide?,
    val size: Pair<Float, Float>,
    var duration: Long?,
    val introAnimationType: AnimationType,
    val introAnimationDuration: Long,
    var outroAnimationType: AnimationType,
    var outroAnimationDuration: Long,
    val variablePlaceHolders: List<String>
) {
    fun toOverlayBlueprint(): OverlayBlueprint {
        val svgData = SvgData(
            svgUrl,
            null
        )
        val viewSpec = ViewSpec(position, size)
        val introTransitionSpec = TransitionSpec(
            offset,
            introAnimationType,
            introAnimationDuration
        )

        val outroTransitionSpec: TransitionSpec =
            if (duration != null && duration!! > 0L) {
                TransitionSpec(
                    offset + duration!!,
                    if (outroAnimationType == AnimationType.UNSPECIFIED) {
                        AnimationType.NONE
                    } else {
                        outroAnimationType
                    },
                    outroAnimationDuration
                )
            } else {
                TransitionSpec(
                    -1L,
                    AnimationType.UNSPECIFIED,
                    -1L
                )
            }



        return OverlayBlueprint(
            id,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            variablePlaceHolders
        )

    }
}