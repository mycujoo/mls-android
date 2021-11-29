package tv.mycujoo.mcls.tv.api

import com.google.android.exoplayer2.ExoPlayer
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.mcls.analytic.YouboraClient
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.player.MediaFactory
import java.util.*
import javax.inject.Inject

class MLSTvInternalBuilder @Inject constructor(
    val mediaFactory: MediaFactory,
    val reactorSocket: IReactorSocket,
    val prefManager: IPrefManager,
    val logger: Logger
) {

    val ima: IIma? = null
    private var logLevel = logger.getLogLevel()

    companion object {
        @JvmStatic
        private var uuid: String? = null
    }


    init {
        uuid = prefManager.get("UUID") ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(uuid!!)

        ima?.setAdsLoaderProvider(
            mediaFactory.defaultMediaSourceFactory
        )
    }

    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get("UUID")
        if (storedUUID == null) {
            prefManager.persist("UUID", uuid)
        }
    }

    /**
     * internal use: create YouboraClient
     * @see YouboraClient
     */
    fun createYouboraClient(plugin: Plugin): YouboraClient {

        val youboraClient = YouboraClient(getUuid(), plugin)
        when (logLevel) {
            LogLevel.MINIMAL -> {
                YouboraLog.setDebugLevel(YouboraLog.Level.SILENT)
            }
            LogLevel.INFO -> {
                YouboraLog.setDebugLevel(YouboraLog.Level.DEBUG)
            }
            LogLevel.VERBOSE -> {
                YouboraLog.setDebugLevel(YouboraLog.Level.VERBOSE)
            }
        }

        return youboraClient
    }

    /**
     * Fail Safe way to grab a Uuid.
     */
    fun getUuid(): String {
        var uuid = MLSTvInternalBuilder.uuid
        if (uuid == null) {
            uuid = prefManager.get(C.UUID_PREF_KEY) ?: UUID.randomUUID().toString()
            persistUUIDIfNotStoredAlready(uuid)
        }
        return uuid
    }

    /**
     * create Exoplayer2Adapter which will act as core player
     * @return Exoplayer2Adapter
     */
    fun createExoPlayerAdapter(exoPlayer: ExoPlayer): Exoplayer2Adapter {
        return Exoplayer2Adapter(exoPlayer)
    }
}
