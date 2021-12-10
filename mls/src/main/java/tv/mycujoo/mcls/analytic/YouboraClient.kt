package tv.mycujoo.mcls.analytic

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.google.android.exoplayer2.ExoPlayer
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.utils.UuidUtils
import javax.inject.Inject

/**
 * Integration with Youbora, the analytical tool.
 * @param logger to log events for developers.
 * @param uuidUtils grants access to Uuid for any given session
 */
class YouboraClient @Inject constructor(
    private val logger: Logger,
    private val uuidUtils: UuidUtils
) : AnalyticsClient {

    var plugin: Plugin? = null

    /**
     * Only AnalyticsClient should know about the implementation of the analytics server and libs
     * This ensures only this class knows about youbora
     */
    fun setYouboraPlugin(
        activity: Activity,
        exoPlayer: ExoPlayer,
        accountCode: String
    ) {
        val youboraOptions = Options()
        youboraOptions.accountCode = accountCode
        youboraOptions.isAutoDetectBackground = true

        plugin = Plugin(youboraOptions, activity.baseContext)

        plugin?.activity = activity
        plugin?.adapter = Exoplayer2Adapter(exoPlayer)

        when (logger.getLogLevel()) {
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
    }

    /**
     * log an Event to Youbora.
     */
    override fun logEvent(event: EventEntity?, live: Boolean) {
        val savedPlugin = plugin
        if (savedPlugin == null) {
            logger.log(MessageLevel.ERROR, "Please Set Plugin Before Logging Event!!")
            return
        }
        if (event == null) {
            logger.log(MessageLevel.ERROR, "event is null")
            return
        }
        savedPlugin.options.username = uuidUtils.getUuid()
        savedPlugin.options.contentTitle = event.title
        savedPlugin.options.contentResource = event.streams.firstOrNull()?.toString()
        savedPlugin.options.contentIsLive = live


        savedPlugin.options.contentCustomDimension2 = event.id
        savedPlugin.options.contentCustomDimension14 = getVideoSource(event)

        savedPlugin.options.contentCustomDimension15 = event.streams.firstOrNull()?.id
    }

    /**
     * activate analytical plugin
     */
    override fun start() {
        plugin?.adapter?.fireResume()
    }


    /**
     * deactivate analytical plugin
     */
    override fun stop() {
        plugin?.fireStop()
    }

    /**region Internal*/
    private fun getVideoSource(event: EventEntity): String {
        return if (event.isNativeMLS) {
            MLS_SOURCE
        } else {
            NONE_NATIVE_SOURCE
        }
    }

    /**endregion */

    companion object {
        const val MLS_SOURCE = "MLS"
        const val NONE_NATIVE_SOURCE = "NonNativeMLS"
    }

    @VisibleForTesting
    fun attachPlugin(plugin: Plugin) {
        this.plugin = plugin
    }
}