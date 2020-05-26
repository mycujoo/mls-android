package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.actions.ActionWrapper

class AnnotationBuilderImpl(private val publisher: AnnotationPublisher) : AnnotationBuilder() {

    private var currentTime: Long = 0L
    private val pendingAnnotationDataSource = ArrayList<AnnotationSourceData>()
    private val pendingActions = ArrayList<ActionWrapper>()


    override fun addPendingAnnotations(pendingAnnotationList: List<AnnotationSourceData>) {
        pendingAnnotationDataSource.addAll(pendingAnnotationList)
    }

    override fun addPendingActions(actions: List<ActionWrapper>) {
        pendingActions.addAll(actions)
    }


    override fun setCurrentTime(time: Long) {
        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time")
        currentTime = time
    }

    override fun buildPendings() {
        pendingAnnotationDataSource.filter { sourceData -> isInRange(sourceData) }
            .forEach { annotation ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings() for Annotations"
                )
                publisher.onNewAnnotationAvailable(
                    annotation
                )
            }


        pendingActions.filter { actionWrapper -> isInRange(actionWrapper) }
            .forEach { actionWrapper ->
                println(
                    "MLS-App AnnotationBuilderImpl - buildPendings() for Actions"
                )
                publisher.onNewActionWrapperAvailable(
                    actionWrapper
                )
            }


    }


    private fun isInRange(annotationSourceData: AnnotationSourceData): Boolean {
        return (annotationSourceData.streamOffset >= currentTime) && (annotationSourceData.streamOffset < currentTime + 1000L)
    }

    private fun isInRange(actionWrapper: ActionWrapper): Boolean {
        return (actionWrapper.offset >= currentTime) && (actionWrapper.offset < currentTime + 1000L)
    }
}