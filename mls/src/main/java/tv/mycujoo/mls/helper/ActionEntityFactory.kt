package tv.mycujoo.mls.helper

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.NEWActionEntity

class ActionEntityFactory {

    companion object {
        fun create(newActionEntity: NEWActionEntity): ActionEntity {
            return ActionEntity(
                newActionEntity.id,
                newActionEntity.offset,
                newActionEntity.type,
                newActionEntity.customId,
                newActionEntity.svgUrl,
                null,
                newActionEntity.positionGuide,
                Pair(
                    newActionEntity.sizePair.first,
                    newActionEntity.sizePair.second
                ),
                newActionEntity.duration,
                newActionEntity.introAnimationType,
                newActionEntity.introAnimationDuration,
                newActionEntity.outroAnimationType,
                newActionEntity.outroAnimationDuration
            )
        }
    }
}