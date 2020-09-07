package tv.mycujoo.mls.core

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.ActionObject

interface IAnnotationFactory {
    fun setAnnotations(annotationList: ActionResponse)
    fun build(currentPosition: Long, isPlaying: Boolean, interrupted: Boolean)

    fun actionList(): List<ActionObject>
}