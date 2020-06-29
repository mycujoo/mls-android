package tv.mycujoo

import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.domain.entity.ShowOverlayActionEntity

fun getShowOverlayActionEntity(offset: Long): ShowOverlayActionEntity {
    return ShowOverlayActionEntity(
        "random_id_1001",
        "custom_id_1001",
        null,
        Pair(300F, 150F),
        offset
    )
}

fun getHideOverlayActionEntity(offset: Long): HideOverlayActionEntity {
    return HideOverlayActionEntity("random_id_1002", "custom_id_1001")
}