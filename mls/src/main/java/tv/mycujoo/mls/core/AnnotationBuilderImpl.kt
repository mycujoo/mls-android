package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData

class AnnotationBuilderImpl(private val publisher: AnnotationPublisher) : AnnotationBuilder() {

    private var currentTime: Long = 0L
    private val pendingAnnotationDataSource = ArrayList<AnnotationSourceData>()


    override fun addPendingAnnotations(pendingAnnotationList: List<AnnotationSourceData>) {
        pendingAnnotationDataSource.addAll(pendingAnnotationList)
    }


    override fun setCurrentTime(time: Long) {
        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time")
        currentTime = time
    }

    override fun buildPendings() {
        pendingAnnotationDataSource.filter { sourceData -> isInRange(sourceData) }
            .forEach { annotation ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings()"
                )
                publisher.onNewAnnotationAvailable(
                    annotation
                )
            }
    }

    private fun isInRange(annotationSourceData: AnnotationSourceData): Boolean {
        return (annotationSourceData.streamOffset >= currentTime) && (annotationSourceData.streamOffset < currentTime + 1000L)

    }
}