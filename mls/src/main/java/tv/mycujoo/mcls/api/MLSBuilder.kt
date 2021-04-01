package tv.mycujoo.mcls.api

import android.app.Activity
import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mcls.cast.ICast
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.core.PlayerEventsListener
import tv.mycujoo.mcls.core.UIEventListener
import tv.mycujoo.mcls.enum.C.Companion.ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.network.socket.ReactorCallback
import tv.mycujoo.mcls.network.socket.ReactorListener

open class MLSBuilder {


    lateinit var internalBuilder: InternalBuilder

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
    internal var mCast: ICast? = null
        private set
    internal var ima: IIma? = null
        private set
    internal var hasAnalytic: Boolean = true
        private set

    fun publicKey(publicKey: String) = apply {
        if (publicKey == "YOUR_PUBLIC_KEY_HERE") {
            throw IllegalArgumentException(PUBLIC_KEY_MUST_BE_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.publicKey = publicKey
    }

    fun withActivity(activity: Activity) = apply {
        this.activity = activity
    }

    fun setPlayerEventsListener(playerEventsListener: tv.mycujoo.mcls.api.PlayerEventsListener) =
        apply { this.playerEventsListener = PlayerEventsListener(playerEventsListener) }

    fun setUIEventListener(uiEventListener: UIEventListener) =
        apply { this.uiEventListener = uiEventListener }

    fun setConfiguration(mlsConfiguration: MLSConfiguration) = apply {
        this.mlsConfiguration = mlsConfiguration
    }

    fun setCast(cast: ICast) = apply {
        this.mCast = cast
    }

    fun setIma(ima: IIma) = apply {
        if (activity == null) {
            throw IllegalArgumentException(ACTIVITY_IS_NOT_SET_IN_MLS_BUILDER_MESSAGE)
        }
        this.ima = ima.apply {
            createAdsLoader(activity!!)
        }
    }

    fun createExoPlayer(context: Context): SimpleExoPlayer? {
        return SimpleExoPlayer.Builder(context).build()
    }

    fun createReactorListener(reactorCallback: ReactorCallback): ReactorListener {
        return ReactorListener(reactorCallback)
    }

    open fun build(): MLS {
        internalBuilder = InternalBuilder(activity!!, ima, mlsConfiguration.logLevel)
        internalBuilder.initialize()

        val mls = MLS(this)
        mls.initializeComponent(internalBuilder)
        return mls
    }

}