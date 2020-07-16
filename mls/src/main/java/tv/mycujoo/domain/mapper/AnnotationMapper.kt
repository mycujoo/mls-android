package tv.mycujoo.domain.mapper

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.NEWActionEntity

class AnnotationMapper {

    companion object {
        fun mapToNEWActionEntity(actionResponse: ActionResponse): List<NEWActionEntity> {

            return actionResponse.data.map { actionSourceData ->
                ActionMapper.mapToEntity(actionSourceData)
            }

        }
    }
}