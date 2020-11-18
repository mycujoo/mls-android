package tv.mycujoo.mls.core

import android.app.Activity
import android.content.res.AssetManager
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.gms.cast.framework.CastContext
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
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.MainWebSocketListener
import tv.mycujoo.mls.network.socket.ReactorSocket
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

    lateinit var reactorSocket: IReactorSocket
    private lateinit var mainWebSocketListener: MainWebSocketListener

    lateinit var castContext: CastContext

    var uuid: String? = null


    open fun initialize() {
        val dependencyGraph =
            DaggerMlsComponent.builder().networkModule(NetworkModule(activity)).build()
        dependencyGraph.inject(this)

        logger = Logger(logLevel)

        viewHandler = ViewHandler(dispatcher, CountingIdlingResource("ViewIdentifierManager"))

        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)

        castContext = CastContext.getSharedInstance(activity)
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
