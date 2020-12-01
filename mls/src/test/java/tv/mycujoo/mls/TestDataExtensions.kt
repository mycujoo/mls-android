package tv.mycujoo.mls

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData

fun OverlayEntity.toActionObject(offset: Long, duration : Long): ActionObject {


    val parsedOverlayRelatedData = ParsedOverlayRelatedData(
        id,
        "",
        duration,
        PositionGuide(),
        Pair(-1F, -1F),
        introTransitionSpec.animationType,
        introTransitionSpec.animationDuration,
        outroTransitionSpec.animationType,
        outroTransitionSpec.animationDuration,
        emptyList()
    )

    return ActionObject(
        id,
        ActionType.SHOW_OVERLAY,
        offset,
        -1L,
        parsedOverlayRelatedData,
        null,
        null
    )
}