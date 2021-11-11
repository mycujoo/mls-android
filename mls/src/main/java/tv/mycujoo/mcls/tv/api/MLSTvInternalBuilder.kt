package tv.mycujoo.mcls.tv.api

import android.content.Context
import com.google.android.exoplayer2.MediaItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.MainWebSocketListener
import tv.mycujoo.mcls.network.socket.ReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.player.Player
import java.util.*
import javax.inject.Inject

class MLSTvInternalBuilder @Inject constructor(
    @ApplicationContext private val context: Context,
    val logger: Logger,
    val viewHandler: IViewHandler,
    val mediaFactory: MediaFactory,
    val reactorSocket: IReactorSocket
) {

    val ima: IIma? = null

    val logLevel: LogLevel = MLSConfiguration().logLevel

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

    var uuid: String? = null


    init {
        logger.setLogLevel(logLevel)

        uuid = prefManager.get("UUID") ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(uuid!!)

        ima?.setAdsLoaderProvider(
            mediaFactory.defaultMediaSourceFactory
        )

        reactorSocket.setUUID(uuid!!)
    }

    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get("UUID")
        if (storedUUID == null) {
            prefManager.persist("UUID", uuid)
        }
    }

}
