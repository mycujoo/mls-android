package tv.mycujoo.mcls.tv.api

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.npaw.ima.ImaAdapter
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import timber.log.Timber
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.analytic.VideoAnalyticsCustomData
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.di.*
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.utils.DeviceUtils
import tv.mycujoo.ui.MLSTVFragment
import javax.inject.Inject
import javax.inject.Singleton

open class MLSTvBuilder {

    private var injected = false

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    internal lateinit var mlsTvFragment: MLSTVFragment
    private var analyticsAccount: String = ""

    internal var pseudoUserId: String? = null
        private set
    internal var userId: String? = null
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
    internal var deviceType: String? = null
        private set
    internal var coroutineScope: CoroutineScope? = null
        private set
    internal var context: Context? = null
    internal var videoAnalyticsCustomData: VideoAnalyticsCustomData? = null
    internal var onConcurrencyLimitExceeded: ((Int) -> Unit)? = null
        private set
    internal var concurrencyLimitFeatureEnabled = true

    internal var onError: ((String) -> Unit)? = null
        private set

    @Inject
    internal lateinit var mlsTV: MLSTV

    @Inject
    internal lateinit var imaAnalyticsAdapter: ImaAdapter

    fun setCoroutinesScope(coroutineScope: CoroutineScope) = apply {
        this.coroutineScope = coroutineScope
    }

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }


    fun setOnError(onError: (String) -> Unit) = apply {
        this.onError = onError
    }

    fun deviceType(deviceType: String) = apply {
        this.deviceType = deviceType
    }

    fun customPseudoUserId(pseudoUserId: String) = apply {
        this.pseudoUserId = pseudoUserId
    }

    fun userId(userId: String) = apply {
        this.userId = userId
    }

    fun withContext(context: Context) = apply {
        this.context = context
    }

    fun identityToken(identityToken: String) = apply {
        this.identityToken = identityToken
    }

    fun withMLSTvFragment(mlsTvFragment: MLSTVFragment) =
        apply { this.mlsTvFragment = mlsTvFragment }

    fun withVideoAnalyticsCustomData(customData: VideoAnalyticsCustomData) = apply {
        this.videoAnalyticsCustomData = customData
    }

    fun setOnConcurrencyLimitExceeded(action: (Int) -> Unit) = apply {
        onConcurrencyLimitExceeded = action
    }

    fun setConcurrencyLimitFeatureEnabled(enabled: Boolean) = apply {
        concurrencyLimitFeatureEnabled = enabled
    }

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

        this.ima = ima
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
        val buildContext =
            context ?: throw Exception(C.CONTEXT_MUST_BE_SET_IN_MLS_TV_BUILDER_MESSAGE)

        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(C.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        val coroutineScope = coroutineScope
        val scope = if (coroutineScope == null) {
            val job = SupervisorJob()
            CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
        } else {
            coroutineScope
        }

        injectIfNeeded(mlsTvFragment.requireActivity(), scope)

        ima?.apply {
            if (hasAnalytic) {
                createAdsLoader(buildContext, imaAnalyticsAdapter)
            } else {
                createAdsLoader(buildContext)
            }
        }

        mlsTV.initialize(this, mlsTvFragment)

        mlsTvFragment.lifecycle.addObserver(mlsTV)

        return mlsTV
    }

    private fun injectIfNeeded(
        activity: FragmentActivity,
        coroutinesScope: CoroutineScope
    ) {
        if (injected) return

        val device = deviceType ?: DeviceUtils.detectTVDeviceType(activity).value

        DaggerMLSTVComponent.builder()
            .withActivity(activity)
            .withContext(activity)
            .withCoroutinesScope(coroutinesScope)
            .withDeviceType(device)
            .withYouboraAccountCode(analyticsAccount)
            .build()
            .inject(this)

        injected = true
    }
}

@Singleton
@Component(
    modules = [
        NetworkModule::class,
        NetworkModuleBinds::class,
        PlayerModule::class,
        AppModuleBinds::class,
        AppModule::class,
        CoreModule::class,
        StorageModule::class,
        AnalyticsModule::class
    ]
)
interface MLSTVComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withActivity(activity: FragmentActivity): Builder

        @BindsInstance
        fun withContext(context: Context): Builder

        @BindsInstance
        fun withCoroutinesScope(coroutineScope: CoroutineScope): Builder

        @BindsInstance
        fun withYouboraAccountCode(@YouboraAccountCode code: String? = null): Builder

        @BindsInstance
        fun withDeviceType(@ClientDeviceType type: String? = null): Builder

        fun build(): MLSTVComponent
    }

    fun inject(builder: MLSTvBuilder)
}