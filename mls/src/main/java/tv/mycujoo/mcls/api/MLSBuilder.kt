package tv.mycujoo.mcls.api

import android.app.Activity
import android.content.pm.PackageManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.cast.ICast
import tv.mycujoo.mcls.core.PlayerEventsListener
import tv.mycujoo.mcls.core.UIEventListener
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C.Companion.ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE
import tv.mycujoo.mcls.ima.IIma
import timber.log.Timber
import tv.mycujoo.mcls.analytic.VideoAnalyticsCustomData
import tv.mycujoo.mcls.enum.LogLevel
import kotlin.math.log


/**
 * builder of MLS(MCLS) main component
 */
open class MLSBuilder {

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private var analyticsAccount: String = ""

    internal var logLevel = LogLevel.MINIMAL
    internal var publicKey: String = ""
        private set
    internal var pseudoUserId: String? = null
        private set
    internal var userId: String? = null
        private set
    internal var customVideoAnalyticsData: VideoAnalyticsCustomData? = null
    internal var identityToken: String = ""
        private set
    internal var activity: Activity? = null
        private set
    internal var playerEventsListener: PlayerEventsListener? = null
        private set
    internal var uiEventListener: UIEventListener? = null
        private set
    internal var mlsConfiguration: MLSConfiguration = MLSConfiguration()
        private set
    var cast: ICast? = null
        private set
    internal var ima: IIma? = null
        private set
    internal var hasAnalytic: Boolean = true
        private set

    /**
     * public-key of user.
     * optional for initializing MLS.
     * @throws IllegalArgumentException if it is NOT set on build
     */
    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }

    fun setLogLevel(logLevel: LogLevel) = apply {
        this.logLevel = logLevel
    }

    fun identityToken(identityToken: String) = apply {
        this.identityToken = identityToken
    }

    fun customPseudoUserId(pseudoUserId: String) = apply {
        this.pseudoUserId = pseudoUserId
    }

    fun userId(userId: String) = apply {
        this.userId = userId
    }

    fun withVideoAnalyticsCustomData(customData: VideoAnalyticsCustomData) = apply {
        this.customVideoAnalyticsData = customData
    }

    /**
     * activity which will be hosting MLS
     */
    fun withActivity(activity: Activity) = apply {
        this.activity = activity
    }

    /**
     * Set Youbora Account Name
     */
    fun setAnalyticsAccount(accountCode: String) = apply {
        this.analyticsAccount = accountCode
    }

    /**
     * set PlayerEventsListener to listen to player core events
     * @see PlayerEventsListener
     */
    fun setPlayerEventsListener(playerEventsListener: tv.mycujoo.mcls.api.PlayerEventsListener) =
        apply { this.playerEventsListener = PlayerEventsListener(playerEventsListener) }

    /**
     * set UIEventListener to listen to player UI events
     * @see UIEventListener
     */
    fun setUIEventListener(uiEventListener: UIEventListener) =
        apply { this.uiEventListener = uiEventListener }

    /**
     * configure MLS video player behaviour and appearance
     * @see MLSConfiguration
     */
    fun setConfiguration(mlsConfiguration: MLSConfiguration) = apply {
        this.mlsConfiguration = mlsConfiguration
    }

    /**
     * attach implementation of ICast to use Google Cast (Chrome Cast) feature
     * in order to provide such a dependency, Cast module must be used
     */
    fun setCast(cast: ICast) = apply {
        this.cast = cast
    }

    /**
     * set implementation of IIma to use Google IMA & display ads.
     * in order to provide such a dependency, IMA module must be used
     */
    fun setIma(ima: IIma) = apply {
        if (activity == null) {
            throw IllegalArgumentException(ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.ima = ima.apply {
            createAdsLoader(activity!!)
        }
    }

    /**
     * creates an account key for analytics.
     * the lib searches for they key in 3 different places
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
    fun getAnalyticsAccountCode(): String {
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
        activity?.applicationContext.let {
            val app = activity?.packageManager?.getApplicationInfo(
                "${it?.packageName}",
                PackageManager.GET_META_DATA
            )
            return app?.metaData?.getString("tv.mycujoo.MLS_ANALYTICS_ACCOUNT") ?: ""
        }
    }

    /**
     * init public key if not present
     */
    protected fun initPublicKeyIfNeeded() {
        // grab public key from Manifest if not set manually,
        if (publicKey.isEmpty()) {
            activity?.applicationContext.let {
                val app = activity?.packageManager?.getApplicationInfo(
                    "${it?.packageName}",
                    PackageManager.GET_META_DATA
                )
                publicKey = app?.metaData?.getString("tv.mycujoo.MLS_PUBLIC_KEY") ?: ""
            }
        }
    }


    /**
     * build MLS with given specification
     * Initializes InternalBuilder and MLS
     * @return MLS
     * @throws IllegalArgumentException if public key was not provided via the Manifest AND not set manually
     */
    open fun build(): MLS {
        // Check if grabbed successfully
        initPublicKeyIfNeeded()
        if (publicKey.isEmpty()) {
            throw IllegalArgumentException(PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }

        val graph = DaggerMLSApplication_HiltComponents_SingletonC.builder()
            .applicationContextModule(ApplicationContextModule(activity))
            .networkModule(NetworkModule())
            .appModule(AppModule())
            .build()

        val mls = graph.provideMLS()
        mls.initializeComponent(this)

        return mls
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BuilderProvider {
        fun provideMLS(): MLS
    }


    companion object {
        private const val TAG = "MLSBuilder"
    }
}