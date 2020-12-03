package tv.mycujoo.cast

class EmptyMLSCastOptionsProvider : MLSCastOptionsProviderAbstract() {
    override fun getReceiverAppId(): String {
        return SAMPLE_APP_ID
    }

    companion object {
        const val SAMPLE_APP_ID = "sample_app_id"
    }
}