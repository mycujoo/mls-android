package tv.mycujoo.domain.mapper

import tv.mycujoo.domain.entity.AnnotationSourceData
import tv.mycujoo.domain.entity.NEWActionEntity
import tv.mycujoo.domain.entity.NEWAnnotationEntity

class AnnotationMapper {

    companion object {
        fun mapToEntity(annotationSourceData: AnnotationSourceData): NEWAnnotationEntity? {

            val id = annotationSourceData.id ?: return null
            val offset = annotationSourceData.offset ?: return null
            val timeline_id = annotationSourceData.timeline_id ?: return null

            val actionList = ArrayList<NEWActionEntity>()
            annotationSourceData.actions?.forEach { actionSourceData ->
                if (actionSourceData == null) {
                    return null
                }
                actionList.add(ActionMapper.mapToEntity(offset, actionSourceData))
            }

            return NEWAnnotationEntity(id, offset, timeline_id, actionList.toList())
        }
    }
}