package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.AnnotationBundle
import tv.mycujoo.mls.model.AnnotationDataSource

class AnnotationBuilderImpl(private val publisher: AnnotationPublisher) : AnnotationBuilder {

    private var currentTime: Long = 0L
    private val pendingAnnotationDataSource = ArrayList<AnnotationDataSource>()

    override fun addPendingAnnotations(annotationDataSourceList: List<AnnotationDataSource>) {
        pendingAnnotationDataSource.addAll(annotationDataSourceList)
    }

    override fun buildAnnotation(annotationDataSource: AnnotationDataSource) {
        val annotationBundle =
            AnnotationBundle(annotationDataSource.type, annotationDataSource.overlayData)
        publisher.onNewAnnotationAvailable(annotationBundle)
    }

    override fun setCurrentTime(time: Long) {
        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time")
        currentTime = time
    }

    override fun buildPendings() {
        pendingAnnotationDataSource.filter { annotationDataSource -> isInRange(annotationDataSource.time) }
            .forEach { annotation ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings() time:${annotation.time}, text:${annotation.overlayData.primaryText}"
                )
                publisher.onNewAnnotationAvailable(
                    AnnotationBundle(
                        annotation.type,
                        annotation.overlayData
                    )
                )
            }
    }

    private fun isInRange(annotationTime: Long): Boolean {
        return (annotationTime >= currentTime) && (annotationTime < currentTime + 1000L)
    }
}