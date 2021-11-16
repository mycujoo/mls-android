package tv.mycujoo.mcls.core

import android.content.Context
import android.content.res.AssetManager
import com.google.android.exoplayer2.ExoPlayer
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Plugin
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext private val context: Context,
    val logger: Logger,
    val mediaFactory: MediaFactory,
    val plugin: Plugin,
    val prefManager: IPrefManager,
) {

    private var ima: IIma? = null
    private var logLevel: LogLevel = MLSConfiguration().logLevel

    /**region Fields*/

    var uuid: String? = null
        private set

    /**endregion */


    /**
     * initialize internal builder and prepare it for usage by MLS
     */
    open fun initialize() {
        uuid = prefManager.get(C.UUID_PREF_KEY) ?: UUID.randomUUID().toString()
        persistUUIDIfNotStoredAlready(uuid!!)

        logger.setLogLevel(logLevel)

        ima?.setAdsLoaderProvider(mediaFactory.defaultMediaSourceFactory)
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
