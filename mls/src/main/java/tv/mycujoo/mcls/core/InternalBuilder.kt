package tv.mycujoo.mcls.core

import com.google.android.exoplayer2.ExoPlayer
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.mcls.analytic.YouboraClient
import tv.mycujoo.mcls.api.MLSConfiguration
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.LogLevel.*
import tv.mycujoo.mcls.ima.IIma
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.player.MediaFactory
import java.util.*
import javax.inject.Inject

/**
 * Internal builder which builds & prepares lower level components for MLS
 */
open class InternalBuilder @Inject constructor(
    val logger: Logger,
    val mediaFactory: MediaFactory,
    val plugin: Plugin,
    val prefManager: IPrefManager,
) {

    private var ima: IIma? = null
    private var logLevel: LogLevel = MLSConfiguration().logLevel

    /**region Fields*/

    companion object {
        @JvmStatic
        private var uuid: String? = null
    }

    /**
     * Fail Safe way to grab a Uuid.
     */
    fun getUuid(): String {
        var uuid = InternalBuilder.uuid
        if (uuid == null) {
            uuid = prefManager.get(C.UUID_PREF_KEY) ?: UUID.randomUUID().toString()
            persistUUIDIfNotStoredAlready(uuid)
        }
        return uuid
    }
    /**endregion */


    /**
     * initialize internal builder and prepare it for usage by MLS
     */
    init {
        uuid = prefManager.get(C.UUID_PREF_KEY) ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(uuid!!)

        logger.setLogLevel(logLevel)

        ima?.setAdsLoaderProvider(mediaFactory.defaultMediaSourceFactory)
    }

    /**
     * internal use: create YouboraClient
     * @see YouboraClient
     */
    fun createYouboraClient(): YouboraClient {
        // Reassign if lost due to scoping
        if (uuid == null) {
            uuid = prefManager.get(C.UUID_PREF_KEY)
        }

        assert(uuid != null) {
            "UUID is Null!!"
        }
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
     * create Exoplayer2Adapter which will act as core player
     * @return Exoplayer2Adapter
     */
    fun createExoPlayerAdapter(exoPlayer: ExoPlayer): Exoplayer2Adapter {
        return Exoplayer2Adapter(exoPlayer)
    }

    /**
     * store UUID in shared pref, if it's NOT already stored
     * @param uuid user's unique identifier
     */
    private fun persistUUIDIfNotStoredAlready(uuid: String) {
        val storedUUID = prefManager.get(C.UUID_PREF_KEY)
        if (storedUUID == null) {
            prefManager.persist(C.UUID_PREF_KEY, uuid)
        }
    }
}
