package tv.mycujoo.mcls.network.socket

interface ConcurrencyCallback {

    fun onBadRequest(reason: String)

    fun onServerError()

    fun onLimitExceeded()
}
