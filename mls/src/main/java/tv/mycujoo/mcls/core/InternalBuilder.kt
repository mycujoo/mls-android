package tv.mycujoo.mcls.core

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mcls.analytic.YouboraClient
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.LogLevel.*
import tv.mycujoo.mcls.helper.AnimationFactory
import tv.mycujoo.mcls.helper.OverlayFactory
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.*
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.MainWebSocketListener
import tv.mycujoo.mcls.network.socket.ReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.player.Player
import javax.inject.Inject

/**
 * Internal builder which builds & prepares lower level components for MLS
 */
open class InternalBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private var ima: IIma? = null
    private var logLevel: LogLevel = MLSConfiguration().logLevel

    /**region Fields*/
    lateinit var logger: Logger

    @Inject
    lateinit var eventsRepository: tv.mycujoo.domain.repository.EventsRepository

    @Inject
    lateinit var dispatcher: CoroutineScope

    @Inject
    lateinit var okHttpClient: OkHttpClient

    @Inject
    lateinit var dataManager: IDataManager

    @Inject
    lateinit var prefManager: IPrefManager

    lateinit var viewHandler: IViewHandler
    lateinit var overlayViewHelper: OverlayViewHelper
    lateinit var variableTranslator: VariableTranslator
    lateinit var variableKeeper: VariableKeeper

    internal lateinit var mediaFactory: MediaFactory

    lateinit var reactorSocket: IReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener

    var uuid: String? = null
    /**endregion */


    /**
     * initialize internal builder and prepare it for usage by MLS
     */
    open fun initialize() {

        logger = Logger(logLevel)

        viewHandler = ViewHandler(CountingIdlingResource("ViewIdentifierManager"))
        variableTranslator = VariableTranslator(dispatcher)
        variableKeeper = VariableKeeper(dispatcher)

        overlayViewHelper = OverlayViewHelper(
            viewHandler,
            OverlayFactory(),
            AnimationFactory(),
            variableTranslator,
            variableKeeper
        )

        mediaFactory = MediaFactory(
            Player.createDefaultMediaSourceFactory(context),
            Player.createMediaFactory(context),
            MediaItem.Builder()
        )

        ima?.let {
            it.setAdsLoaderProvider(mediaFactory.defaultMediaSourceFactory)
        }

        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)

    }

    /**
     * internal use: AssetManager of provided activity
     * @return AssetManager
     */
    fun getAssetManager(): AssetManager = context.assets

    /**
     * internal use: create YouboraClient
     * @see YouboraClient
     */
    fun createYouboraClient(plugin: Plugin): YouboraClient {
        val youboraClient = YouboraClient(uuid!!, plugin)
        when (logLevel) {
            MINIMAL -> {
                YouboraLog.setDebugLevel(YouboraLog.Level.SILENT)
            }
            INFO -> {
                YouboraLog.setDebugLevel(YouboraLog.Level.DEBUG)
            }
            VERBOSE -> {
                YouboraLog.setDebugLevel(YouboraLog.Level.VERBOSE)
            }
        }

        return youboraClient
    }

    /**
     * internal use: create Youbora Plugin
     * this plugin and Youbora Client will work together to send video related analytics
     * @see Plugin
     */
    fun createYouboraPlugin(youboraOptions: Options, context: Context): Plugin {
        return Plugin(youboraOptions, context)
    }

    /**
     * create Exoplayer2Adapter which will act as core player
     * @return Exoplayer2Adapter
     */
    fun createExoPlayerAdapter(exoPlayer: ExoPlayer): Exoplayer2Adapter {
        return Exoplayer2Adapter(exoPlayer)
    }
}
