package tv.mycujoo.mls.network.socket

interface IReactorSocket {

    fun addListener(reactorCallback: ReactorCallback)

    fun connect(eventId: String)
    fun disconnect(eventId: String)

}
