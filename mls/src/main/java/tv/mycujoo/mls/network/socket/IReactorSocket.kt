package tv.mycujoo.mls.network.socket

import tv.mycujoo.mls.model.JoinTimelineParam

interface IReactorSocket {

    fun addListener(reactorCallback: ReactorCallback)

    fun setUUID(uuid: String)
    fun joinEvent(eventId: String)
    fun leave(destroyAfter: Boolean)
    fun joinTimelineIfNeeded(param: JoinTimelineParam)
}
