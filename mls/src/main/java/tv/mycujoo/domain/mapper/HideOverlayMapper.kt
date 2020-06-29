package tv.mycujoo.domain.mapper

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.HideOverlayActionEntity

class HideOverlayMapper {

    companion object {
        fun mapToEntity(actionEntity: ActionEntity): HideOverlayActionEntity {
            return HideOverlayActionEntity(
                actionEntity.id,
                actionEntity.customId
            )
        }

    }
}