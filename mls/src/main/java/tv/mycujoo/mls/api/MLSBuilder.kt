package tv.mycujoo.mls.api

import android.app.Activity
import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mls.cast.ICast
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener
import tv.mycujoo.mls.ima.IIma
import tv.mycujoo.mls.network.socket.ReactorCallback
import tv.mycujoo.mls.network.socket.ReactorListener

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
            throw IllegalArgumentException("Public key must be set!")
        }
        this.publicKey = publicKey
    }

    fun withActivity(activity: Activity) = apply { this.activity = activity }

    fun setPlayerEventsListener(playerEventsListener: tv.mycujoo.mls.api.PlayerEventsListener) =
        apply { this.playerEventsListener = PlayerEventsListener(playerEventsListener) }

    fun setUIEventListener(uiEventListener: UIEventListener) =
        apply { this.uiEventListener = uiEventListener }

    fun setConfiguration(mlsConfiguration: MLSConfiguration) = apply {
        this.mlsConfiguration = mlsConfiguration
    }

    fun setCast(cast: ICast) = apply {
        this.mCast = cast
    }

    fun ima(ima: IIma) = apply {
        this.ima = ima
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