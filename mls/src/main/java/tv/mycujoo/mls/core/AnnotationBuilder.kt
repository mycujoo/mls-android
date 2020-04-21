package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.AnnotationDataSource

interface AnnotationBuilder {
    fun buildAnnotation(annotationDataSource: AnnotationDataSource)
}