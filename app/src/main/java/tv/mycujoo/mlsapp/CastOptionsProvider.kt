package tv.mycujoo.mlsapp

import tv.mycujoo.cast.MLSCastOptionsProviderAbstract

class CastOptionsProvider : MLSCastOptionsProviderAbstract() {

    override fun getReceiverAppId(): String {
        return "RECEIVER_APP_ID_HERE"
    }
}