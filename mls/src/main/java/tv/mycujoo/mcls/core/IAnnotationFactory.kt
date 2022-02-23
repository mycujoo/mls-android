package tv.mycujoo.mcls.core

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mcls.api.PlayerViewContract

interface IAnnotationFactory {
    fun setActions(actions: List<Action>)
    fun setMCLSActions(annotations: List<Action>)

    fun attachPlayerView(playerView: PlayerViewContract)
    fun build()

    fun getCurrentActions(): List<Action>

    fun clearOverlays()
}
