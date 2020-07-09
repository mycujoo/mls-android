package tv.mycujoo

import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.ShowOverlayActionEntity

fun getShowOverlayActionEntity(offset: Long): ShowOverlayActionEntity {
    return ShowOverlayActionEntity(
        "random_id_1001",
        "custom_id_1001",
        null,
        PositionGuide(1F),
        Pair(60F, 0F),
        offset,
        AnimationType.NONE,
        -1L,
        AnimationType.NONE,
        -1L
    )
}

fun getShowOverlayActionEntity(
    offset: Long,
    positionGuide: PositionGuide
): ShowOverlayActionEntity {
    return ShowOverlayActionEntity(
        "random_id_1001",
        "custom_id_1001",
        null,
        positionGuide,
        Pair(30F, 0F),
        offset,
        AnimationType.NONE,
        -1L,
        AnimationType.NONE,
        -1L
    )
}

fun getHideOverlayActionEntity(offset: Long): HideOverlayActionEntity {
    return HideOverlayActionEntity(
        "random_id_1002", "custom_id_1001",
        AnimationType.NONE,
        -1L
    )
}