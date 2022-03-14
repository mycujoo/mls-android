package tv.mycujoo.mcls.network.socket

interface ConcurrencyCallback {

    fun onOK()

    fun onMissingIdentifier()

    fun onForbidden()

    fun onNoEntitlement()

    fun onInternalError()

    fun onInvalidCommand()

    fun onUnknownError()

    fun onLimitExceeded()
}
