package tv.mycujoo.mls.network.socket

interface IReactorSocket {

    fun addListener(reactorCallback: ReactorCallback)

    fun setUUID(uuid: String)
    fun join(eventId: String)
    fun leave(destroyAfter: Boolean)
}
