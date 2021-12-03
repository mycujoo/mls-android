package tv.mycujoo.mcls.tv.api

import android.content.pm.PackageManager
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.ui.MLSTVFragment
import java.lang.IllegalStateException

open class MLSTvBuilder {

    internal lateinit var mlsTvFragment: MLSTVFragment
    private var analyticsAccount: String = ""

    internal lateinit var youboraPlugin: Plugin
        private set
    internal var publicKey: String = ""
        private set
    internal var identityToken: String = ""
        private set
    internal var mlsTVConfiguration: MLSTVConfiguration = MLSTVConfiguration()
        private set
    internal var ima: IIma? = null
        private set
    internal var logLevel: LogLevel = LogLevel.MINIMAL
        private set
    internal var hasAnalytic: Boolean = true
        private set

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }

    fun identityToken(identityToken: String) = apply {
        this.identityToken = identityToken
    }

    fun withMLSTvFragment(mlsTvFragment: MLSTVFragment) =
        apply { this.mlsTvFragment = mlsTvFragment }

    /**
     * create Youbora Plugin.
     * To Initiate the Library, the lib searches for they key in 3 different places
     *
     *  1. If Youbora Code was Provided with analyticsAccount(String),
     *     Then use it
     *
     *  2. If Above Fails,
     *      Then use Code Provided by the Android Manifest via tag:
     *
     *          <meta-data
     *              android:name="tv.mycujoo.MLS_ANALYTICS_ACCOUNT"
     *              android:value="MLS_ACCOUNT_CODE_HERE" />
     *
     *  3. else,
     *      Then use MyCujoo Default Account Name
     */
    protected fun initYouboraPlugin() {
        // Provided via the Builder
        var code = analyticsAccount

        // Provided from the Manifest
        if (code.isEmpty()) {
            code = grabAnalyticsKeyFromManifest()
        }

        // MyCujoo Account Code
        if (code.isEmpty()) {
            code = BuildConfig.MYCUJOO_YOUBORA_ACCOUNT_NAME
        }

        val youboraOptions = Options()
        youboraOptions.accountCode = code
        youboraOptions.isAutoDetectBackground = true

        youboraPlugin = Plugin(youboraOptions, mlsTvFragment.requireActivity().baseContext)
    }

    /**
     *  gets the Youbora Account Name From the AndroidManifest.xml
     */
    private fun grabAnalyticsKeyFromManifest(): String {
        mlsTvFragment.activity?.applicationContext.let {
            val app = mlsTvFragment.activity?.packageManager?.getApplicationInfo(
                "${it?.packageName}",
                PackageManager.GET_META_DATA
            )
            return app?.metaData?.getString("tv.mycujoo.MLS_ANALYTICS_ACCOUNT") ?: ""
        }
    }

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

    open fun build(): MLSTV {
        if (!mlsTvFragment.isResumed) {
            throw IllegalStateException(C.FRAGMENT_MUST_BE_INFLATED_WHEN_BUILDING)
        }

        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        initYouboraPlugin()

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