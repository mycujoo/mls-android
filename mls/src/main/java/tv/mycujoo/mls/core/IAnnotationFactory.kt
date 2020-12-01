package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.ActionObject

interface IAnnotationFactory {
    fun setAnnotations(actionObjectList: List<ActionObject>)
    fun build(buildPoint: BuildPoint)

    fun actionList(): List<ActionObject>
}