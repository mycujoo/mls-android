package tv.mycujoo.mcls.tv.api

import android.app.Activity
import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import javax.inject.Inject

class MLSTvBuilder @Inject constructor(
    val internalBuilder: MLSTvInternalBuilder
) {
    internal var publicKey: String = ""
        private set
    internal var activity: Activity? = null
        private set
    private var mlsTVConfiguration: MLSTVConfiguration = MLSTVConfiguration()
    internal var ima: IIma? = null
        private set
    internal var logLevel: LogLevel = LogLevel.MINIMAL
        private set

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }

    fun withActivity(activity: Activity) = apply { this.activity = activity }

    fun setConfiguration(mlsTVConfiguration: MLSTVConfiguration) = apply {
        this.mlsTVConfiguration = mlsTVConfiguration
    }
    fun ima(ima: IIma) = apply {
        if (activity == null) {
            throw IllegalArgumentException(C.ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.ima = ima.apply {
            createAdsLoader(activity!!)
        }
    }

    fun setLogLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }

    fun build(): MLSTV {
        internalBuilder.prefManager.persist(PUBLIC_KEY_PREF_KEY, publicKey)

        val graph = DaggerMLSApplication_HiltComponents_SingletonC.builder()
            .applicationContextModule(ApplicationContextModule(activity!!.applicationContext))
            .networkModule(NetworkModule())
            .appModule(AppModule())
            .build()


        return graph.provideMLSTV()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface TvEntries {
        fun provideMLSTV(): MLSTV
    }
}