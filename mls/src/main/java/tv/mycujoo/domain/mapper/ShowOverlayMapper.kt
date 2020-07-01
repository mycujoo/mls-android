package tv.mycujoo.domain.mapper

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.ShowOverlayActionEntity

class ShowOverlayMapper {

    companion object {
        fun mapToEntity(actionEntity: ActionEntity): ShowOverlayActionEntity {
            return ShowOverlayActionEntity(
                actionEntity.id,
                actionEntity.customId,
                actionEntity.svgInputStream,
                actionEntity.position!!,
                actionEntity.size,
                actionEntity.duration,
                actionEntity.animationType,
                actionEntity.animationDuration!!
            )
        }

    }
}