package tv.mycujoo.mcls.network.socket

interface IConcurrencySocket {

    fun startSession(eventId: String, identityToken: String?)

    fun leaveCurrentSession()

    fun addListener(concurrencyCallback: ConcurrencyCallback)
}
