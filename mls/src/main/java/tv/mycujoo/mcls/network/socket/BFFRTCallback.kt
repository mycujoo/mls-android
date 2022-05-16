package tv.mycujoo.mcls.network.socket

interface BFFRTCallback {

    fun onBadRequest(reason: String)

    fun onServerError()

    fun onLimitExceeded(allowedDevicesNumber: Int)
}
