package tv.mycujoo.mls.model

import tv.mycujoo.mls.entity.HighlightEntity

class AnnotationMapper {

    companion object {
        fun mapToHighlightEntity(annotationDataSource: AnnotationDataSource): HighlightEntity {
            checkNotNull(annotationDataSource.highlightData)
            annotationDataSource.highlightData.let {
                return HighlightEntity(
                    annotationDataSource.time,
                    it.title,
                    it.timeLabel,
                    it.streamUrl
                )
            }
        }
    }
}