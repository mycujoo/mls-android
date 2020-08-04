package tv.mycujoo.mls.api

import android.app.Activity
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.core.PlayerEventsListener
import tv.mycujoo.mls.core.UIEventListener

open class MLSBuilder {


    lateinit var internalBuilder: InternalBuilder

    internal var publicKey: String = ""
        private set
    internal var activity: Activity? = null
        private set
    internal var hasDefaultController: Boolean = true
        private set
    internal var highlightListParams: HighlightListParams? = null
        private set
    internal var playerEventsListener: PlayerEventsListener? = null
        private set
    internal var uiEventListener: UIEventListener? = null
        private set
    internal var mlsConfiguration: MLSConfiguration = MLSConfiguration()
        private set

    internal var hasAnnotation: Boolean = true
        private set
    internal var hasAnalytic: Boolean = true
        private set

    fun publicKey(publicKey: String) = apply { this.publicKey = publicKey }

    fun withActivity(activity: Activity) = apply { this.activity = activity }

    fun defaultPlayerController(defaultController: Boolean) =
        apply { this.hasDefaultController = defaultController }

    fun highlightList(highlightListParams: HighlightListParams) =
        apply { this.highlightListParams = highlightListParams }

    fun setPlayerEventsListener(playerEventsListener: tv.mycujoo.mls.api.PlayerEventsListener) =
        apply { this.playerEventsListener = PlayerEventsListener(playerEventsListener) }

    fun setUIEventListener(uiEventListener: UIEventListener) =
        apply { this.uiEventListener = uiEventListener }


    fun hasAnnotation(hasAnnotation: Boolean) =
        apply { this.hasAnnotation = hasAnnotation }

    fun hasAnalyticPlugin(hasAnalytic: Boolean) =
        apply { this.hasAnalytic = hasAnalytic }

    open fun build(): MLS {

        internalBuilder = InternalBuilder(activity!!)
        internalBuilder.initialize()

        val mls = MLS(this)
        mls.initialize(internalBuilder)
        return mls
    }

    fun setConfiguration(mlsConfiguration: MLSConfiguration) = apply {
        this.mlsConfiguration = mlsConfiguration
    }

}