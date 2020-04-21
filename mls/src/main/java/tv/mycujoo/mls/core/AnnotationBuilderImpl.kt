package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.AnnotationBundle
import tv.mycujoo.mls.model.AnnotationDataSource

class AnnotationBuilderImpl(private val publisher: AnnotationPublisher) : AnnotationBuilder {

    override fun buildAnnotation(annotationDataSource: AnnotationDataSource) {
        val annotationBundle =
            AnnotationBundle(annotationDataSource.type, annotationDataSource.overlayData)
        publisher.onNewAnnotationAvailable(annotationBundle)
    }
}