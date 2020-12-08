package tv.mycujoo.mls.tv.api

import android.app.Activity
import tv.mycujoo.mls.api.MLSTVConfiguration
import tv.mycujoo.mls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mls.enum.LogLevel

class MLSTvBuilder {
    internal var publicKey: String = ""
        private set
    internal var activity: Activity? = null
        private set
    internal var mlsTVConfiguration: MLSTVConfiguration = MLSTVConfiguration()
        private set
    internal var logLevel: LogLevel = LogLevel.MINIMAL
        private set

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException("Public key must be set!")
        }
        this.publicKey = publicKey
    }

    fun withActivity(activity: Activity) = apply { this.activity = activity }

    fun setConfiguration(mlsTVConfiguration: MLSTVConfiguration) = apply {
        this.mlsTVConfiguration = mlsTVConfiguration
    }

    fun setLogLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }

    open fun build(): MLSTV {
        val internalBuilder = MLSTvInternalBuilder(activity!!, logLevel)
        internalBuilder.prefManager.persist(PUBLIC_KEY_PREF_KEY, publicKey)
        return MLSTV(
            activity!!,
            mlsTVConfiguration,
            internalBuilder.reactorSocket,
            internalBuilder.dispatcher,
            internalBuilder.dataManager,
            internalBuilder.okHttpClient,
            internalBuilder.logger
        )
    }
}