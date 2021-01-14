package tv.mycujoo.mls.core

import android.app.Activity
import android.content.res.AssetManager
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mls.analytic.YouboraClient
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.di.DaggerMlsComponent
import tv.mycujoo.mls.di.NetworkModule
import tv.mycujoo.mls.enum.LogLevel
import tv.mycujoo.mls.enum.LogLevel.*
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.OverlayFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.*
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.MainWebSocketListener
import tv.mycujoo.mls.network.socket.ReactorSocket
import tv.mycujoo.mls.player.MediaFactory
import tv.mycujoo.mls.player.Player
import javax.inject.Inject

open class InternalBuilder(private val activity: Activity, private val logLevel: LogLevel) {

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


    open fun initialize() {
        val dependencyGraph =
            DaggerMlsComponent.builder().networkModule(NetworkModule(activity)).build()
        dependencyGraph.inject(this)

        logger = Logger(logLevel)

        viewHandler = ViewHandler(dispatcher, CountingIdlingResource("ViewIdentifierManager"))
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
            Player.createDefaultMediaSourceFactory(activity),
            Player.createMediaFactory(activity),
            MediaItem.Builder()
        )

        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)

    }

    fun getAssetManager(): AssetManager = activity.assets

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

    fun createYouboraPlugin(youboraOptions: Options, activity: Activity): Plugin {
        return Plugin(youboraOptions, activity)
    }

    fun createExoPlayerAdapter(exoPlayer: ExoPlayer): Exoplayer2Adapter {
        return Exoplayer2Adapter(exoPlayer)
    }
}
