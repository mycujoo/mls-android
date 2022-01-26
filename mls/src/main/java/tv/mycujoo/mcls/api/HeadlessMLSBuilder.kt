package tv.mycujoo.mcls.api

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.MLSApplication_HiltComponents
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.tv.api.HeadlessMLS
import tv.mycujoo.ui.MLSTVFragment

class HeadlessMLSBuilder {
    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    internal lateinit var mlsTvFragment: MLSTVFragment

    internal var publicKey: String = ""
        private set
    internal var identityToken: String = ""
        private set
    internal var logLevel: LogLevel = LogLevel.MINIMAL
        private set
    private var graph: MLSApplication_HiltComponents.SingletonC? = null

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }

    fun identityToken(identityToken: String) = apply {
        this.identityToken = identityToken
    }

    /**
     * init public key if not present
     */
    protected fun initPublicKeyIfNeeded() {
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

    // Headless is a client without UI elements in it.
    open fun build(context: Context): HeadlessMLS {
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        val prefManager = getGraph(context).providePrefsManager()
        prefManager.persist(C.IDENTITY_TOKEN_PREF_KEY, identityToken)
        prefManager.persist(C.PUBLIC_KEY_PREF_KEY, publicKey)

        return getGraph(context).provideMLSHeadless()
    }

    private fun getGraph(applicationContext: Context): MLSApplication_HiltComponents.SingletonC {
        val currentGraph = graph
        return if (currentGraph == null) {
            val newGraph = DaggerMLSApplication_HiltComponents_SingletonC.builder()
                .applicationContextModule(ApplicationContextModule(applicationContext))
                .networkModule(NetworkModule())
                .appModule(AppModule())
                .build()
            graph = newGraph
            newGraph
        } else {
            currentGraph
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface Entries {

        fun provideMLSHeadless(): HeadlessMLS

        fun providePrefsManager(): IPrefManager
    }
}