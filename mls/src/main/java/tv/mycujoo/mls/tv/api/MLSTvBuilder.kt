package tv.mycujoo.mls.tv.api

import android.app.Activity

class MLSTvBuilder {
    internal var publicKey: String = ""
        private set
    internal var activity: Activity? = null
        private set

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException("Public key must be set!")
        }
        this.publicKey = publicKey
    }

    fun withActivity(activity: Activity) = apply { this.activity = activity }

    open fun build(): MLSTV {
        val internalBuilder = MLSTvInternalBuilder(activity!!)
        return MLSTV(activity!!, internalBuilder.prefManager, internalBuilder.dataManager)
    }
}