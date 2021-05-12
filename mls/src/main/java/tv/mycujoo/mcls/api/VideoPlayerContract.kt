package tv.mycujoo.mcls.api

import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mcls.core.PlayerEventsListener
import tv.mycujoo.mcls.core.UIEventListener

interface VideoPlayerContract {

    fun playVideo(event: EventEntity)
    fun playVideo(eventId: String)
    fun setLocalAnnotations(annotations: List<Action>)

    fun setPlayerEventsListener(listener: PlayerEventsListener)
    fun setUIEventListener(listener: UIEventListener)
}
