package tv.mycujoo.mcls.network.socket

import tv.mycujoo.mcls.model.JoinTimelineParam

interface IReactorSocket {

    fun addListener(reactorCallback: ReactorCallback)

    fun joinEvent(eventId: String)
    fun leave(destroyAfter: Boolean)
    fun joinTimeline(param: JoinTimelineParam)
}
