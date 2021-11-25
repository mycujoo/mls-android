package tv.mycujoo.mcls.tv.api

import android.content.pm.PackageManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.ui.MLSTVFragment

class MLSTvBuilder {

    private lateinit var mlsTvFragment: MLSTVFragment

    internal var publicKey: String = ""
        private set
    internal var mlsTVConfiguration: MLSTVConfiguration = MLSTVConfiguration()
        private set
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

    fun withMLSTvFragment(mlsTvFragment: MLSTVFragment) =
        apply { this.mlsTvFragment = mlsTvFragment }

    fun setConfiguration(mlsTVConfiguration: MLSTVConfiguration) = apply {
        this.mlsTVConfiguration = mlsTVConfiguration
    }

    fun ima(ima: IIma) = apply {
        if (mlsTvFragment.activity == null) {
            throw IllegalArgumentException(C.ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.ima = ima.apply {
            createAdsLoader(mlsTvFragment.requireActivity())
        }
    }

    /**
     * init public key if not present
     */
    private fun initPublicKeyIfNeeded() {
        // grab public key from Manifest if not set manually,
        if (publicKey.isEmpty()) {
            mlsTvFragment.requireActivity().applicationContext.let {
                val app = it?.packageManager?.getApplicationInfo(
                    it.packageName,
                    PackageManager.GET_META_DATA
                )
                publicKey = app?.metaData?.getString("tv.mycujoo.MLS_PUBLIC_KEY") ?: ""
            }
        }
    }

    fun setLogLevel(logLevel: LogLevel) = apply { this.logLevel = logLevel }

    fun build(): MLSTV {
        initPublicKeyIfNeeded()

        val graph = DaggerMLSApplication_HiltComponents_SingletonC.builder()
            .applicationContextModule(
                ApplicationContextModule(
                    mlsTvFragment.requireActivity().applicationContext
                )
            )
            .networkModule(NetworkModule())
            .appModule(AppModule())
            .build()

        val mlsTv = graph.provideMLSTV()
        mlsTv.initialize(this, mlsTvFragment)

        return mlsTv
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface TvEntries {
        fun provideMLSTV(): MLSTV
    }
}