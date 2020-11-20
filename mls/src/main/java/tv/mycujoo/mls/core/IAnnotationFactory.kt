package tv.mycujoo.mls.core

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.mls.player.IPlayer

interface IAnnotationFactory {
    fun setAnnotations(annotationList: ActionResponse)
    fun build(currentPosition: Long, player: IPlayer, interrupted: Boolean)

    fun actionList(): List<ActionObject>
}