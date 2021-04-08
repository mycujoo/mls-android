package tv.mycujoo.mcls.tv.api

import android.app.Activity
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.di.DaggerMlsComponent
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.MainWebSocketListener
import tv.mycujoo.mcls.network.socket.ReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.player.Player
import java.util.*
import javax.inject.Inject

class MLSTvInternalBuilder(activity: Activity, ima: IIma?, logLevel: LogLevel) {

    var logger: Logger

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

    internal var mediaFactory: MediaFactory

    var reactorSocket: IReactorSocket
    private var mainWebSocketListener: MainWebSocketListener

    var uuid: String? = null


    init {
        val dependencyGraph =
            DaggerMlsComponent.builder().networkModule(NetworkModule(activity)).build()
        dependencyGraph.inject(this)

        logger = Logger(logLevel)

        uuid = prefManager.get("UUID") ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(uuid!!)

        mediaFactory = MediaFactory(
            Player.createDefaultMediaSourceFactory(activity),
            Player.createMediaFactory(activity),
            MediaItem.Builder()
        )

        ima?.let {
            it.setAdsLoaderProvider(
                mediaFactory.defaultMediaSourceFactory
            )
        }

        mainWebSocketListener = MainWebSocketListener()
        reactorSocket = ReactorSocket(okHttpClient, mainWebSocketListener)
        reactorSocket.setUUID(uuid!!)
    }

    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get("UUID")
        if (storedUUID == null) {
            prefManager.persist("UUID", uuid)
        }
    }

}
