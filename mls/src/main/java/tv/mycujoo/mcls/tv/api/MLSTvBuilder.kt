package tv.mycujoo.mcls.tv.api

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.MLSApplication_HiltComponents
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.ui.MLSTVFragment

open class MLSTvBuilder {

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    internal lateinit var mlsTvFragment: MLSTVFragment
    private var analyticsAccount: String = ""

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
    fun getAnalyticsCode(): String {
        // Provided via the Builder
        if (analyticsAccount.isNotEmpty()) {
            return analyticsAccount
        }

        // If Provided via Manifest
        val manifestAnalyticsCode = grabAnalyticsKeyFromManifest()
        if (manifestAnalyticsCode.isNotEmpty()) {
            return manifestAnalyticsCode
        }

        // Default Value
        return BuildConfig.MYCUJOO_YOUBORA_ACCOUNT_NAME
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

    open fun build(context: Context): MLSTV {
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        val graph = getGraph(context)

        val mlsTv = graph.provideMLSTV()
        mlsTv.initialize(this, mlsTvFragment)

        mlsTvFragment.lifecycle.addObserver(mlsTv)

        return mlsTv
    }


    // Headless is a client without UI elements in it.
    open fun buildHeadless(activity: FragmentActivity): HeadlessMLSTv {
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        val prefManager = getGraph(activity.baseContext).providePrefsManager()
        prefManager.persist(C.IDENTITY_TOKEN_PREF_KEY, identityToken)
        prefManager.persist(C.PUBLIC_KEY_PREF_KEY, publicKey)

        val headlessMLSTv = HeadlessMLSTv(activity.baseContext)
        activity.lifecycle.addObserver(headlessMLSTv)

        return headlessMLSTv
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

    inner class HeadlessMLSTv(val context: Context) : DefaultLifecycleObserver {

        fun getDataManager(): IDataManager {
            return getGraph(context).provideDataManager()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            val prefManager = getGraph(context).providePrefsManager()
            prefManager.delete(C.IDENTITY_TOKEN_PREF_KEY)
            prefManager.delete(C.PUBLIC_KEY_PREF_KEY)
            super.onDestroy(owner)
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface TvEntries {
        fun provideMLSTV(): MLSTV

        fun provideDataManager(): IDataManager

        fun providePrefsManager(): IPrefManager
    }
}
