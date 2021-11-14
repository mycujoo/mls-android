package tv.mycujoo.mcls.api

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.modules.ApplicationContextModule
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.DaggerMLSApplication_HiltComponents_SingletonC
import tv.mycujoo.mcls.MissingKeyException
import tv.mycujoo.mcls.cast.ICast
import tv.mycujoo.mcls.core.PlayerEventsListener
import tv.mycujoo.mcls.core.UIEventListener
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.C.Companion.ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.network.socket.ReactorCallback
import tv.mycujoo.mcls.network.socket.ReactorListener

/**
 * builder of MLS(MCLS) main component
 */
open class MLSBuilder {


    internal var publicKey: String = ""
        private set
    internal var activity: Activity? = null
        private set
    internal var playerEventsListener: PlayerEventsListener? = null
        private set
    internal var uiEventListener: UIEventListener? = null
        private set
    internal var mlsConfiguration: MLSConfiguration = MLSConfiguration()
        private set
    private var mCast: ICast? = null
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

    /**
     * activity which will be hosting MLS
     */
    fun withActivity(activity: Activity) = apply {
        this.activity = activity
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
        this.mCast = cast
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
     * internal use: create instance of Exoplayer
     */
    fun createExoPlayer(context: Context): SimpleExoPlayer {
        return SimpleExoPlayer.Builder(context).build()
    }

    /**
     * internal use: create listener for Reactor service
     * @see ReactorCallback
     * @see ReactorListener
     */
    fun createReactorListener(reactorCallback: ReactorCallback): ReactorListener {
        return ReactorListener(reactorCallback)
    }

    /**
     * build MLS with given specification
     * Initializes InternalBuilder and MLS
     * @return MLS
     */
    open fun build(): MLS {
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

        // Check if grabbed successfully
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