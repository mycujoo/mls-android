package tv.mycujoo.mlsapp

import tv.mycujoo.mls.cast.MLSCastOptionsProviderAbstract
import tv.mycujoo.mlsapp.activity.MainActivity

class CastOptionsProvider : MLSCastOptionsProviderAbstract() {
    override fun getActivityName(): String {
        return MainActivity::class.java.toString()
    }

    override fun getReceiverAppId(): String {
        return "RECEIVER_APP_ID_HERE"
    }
}