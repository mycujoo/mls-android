package tv.mycujoo.mcls.analytic

import androidx.fragment.app.FragmentActivity
import com.npaw.ima.ImaAdapter
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import javax.inject.Inject

/**
 * Integration with Youbora, the analytical tool.
 * @param logger to log events for developers.
 * @param userPreferencesUtils to get user specific params (This is a singleton)
 */
class YouboraClient @Inject constructor(
    private val logger: Logger,
    private val userPreferencesUtils: UserPreferencesUtils,
    private val plugin: Plugin,
    private val player: IPlayer,
    private val activity: FragmentActivity,
    private val imaAdapter: ImaAdapter
) : AnalyticsClient {


    private var videoAnalyticsCustomData: VideoAnalyticsCustomData? = null

    init {
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
     * In case Youbora is needed, we should attach it to the activity and exoplayer.
     * This enabled Youbora to send Events when Analytics is
     */
    fun attachYouboraToPlayer(
        videoAnalyticsCustomData: VideoAnalyticsCustomData? = null,
        imaEnabled: Boolean
    ) {
        this.videoAnalyticsCustomData = videoAnalyticsCustomData

        player.getDirectInstance()?.let { exoPlayer ->
            plugin.activity = activity
            plugin.adapter = Exoplayer2Adapter(exoPlayer)
            if (imaEnabled) {
                plugin.adsAdapter = imaAdapter
            }
        }
    }

    fun getYouboraError(): String? {
        val youboraPlugin = plugin ?: return "YouboraClient: Youbora Plugin was not initialed"

        if (youboraPlugin.username.isNullOrEmpty()) {
            return "YouboraClient: Empty Username"
        }

        if (youboraPlugin.title.isNullOrEmpty()) {
            return "YouboraClient: Empty Title"
        }

        if (youboraPlugin.contentCustomDimension2.isNullOrEmpty() ||
            youboraPlugin.contentCustomDimension14.isNullOrEmpty() ||
            youboraPlugin.contentCustomDimension15.isNullOrEmpty()
        ) {
            return "YouboraClient: Found Null Mandatory Params " +
                    "${youboraPlugin.options.contentCustomDimensions}"
        }

        return null
    }

    /**
     * log an Event to Youbora.
     */
    override fun logEvent(event: EventEntity?, live: Boolean, onError: (String) -> Unit) {
        val savedPlugin = plugin
        if (event == null) {
            onError("YouboraClient: event is null")
            logger.log(MessageLevel.ERROR, "event is null")
            return
        }
        savedPlugin.options.username = userPreferencesUtils.getPseudoUserId()
        savedPlugin.options.contentTitle = event.title
        savedPlugin.options.contentResource = event.streams.firstOrNull()?.toString()

        savedPlugin.options.contentCustomDimension2 = event.id

        savedPlugin.options.contentCustomDimension14 = getVideoSource(event)
        savedPlugin.options.contentCustomDimension15 = event.streams.firstOrNull()?.id

        videoAnalyticsCustomData?.let {
            savedPlugin.options.contentCustomDimension1 = it.contentCustomDimension1
            savedPlugin.options.contentCustomDimension3 = it.contentCustomDimension3
            savedPlugin.options.contentCustomDimension4 = it.contentCustomDimension4
            savedPlugin.options.contentCustomDimension5 = it.contentCustomDimension5
            savedPlugin.options.contentCustomDimension6 = it.contentCustomDimension6
            savedPlugin.options.contentCustomDimension7 = it.contentCustomDimension7
            savedPlugin.options.contentCustomDimension8 = it.contentCustomDimension8
            savedPlugin.options.contentCustomDimension9 = it.contentCustomDimension9
            savedPlugin.options.contentCustomDimension10 = it.contentCustomDimension10
            savedPlugin.options.contentCustomDimension11 = it.contentCustomDimension11
            savedPlugin.options.contentCustomDimension12 = it.contentCustomDimension12
            savedPlugin.options.contentCustomDimension13 = it.contentCustomDimension13
        }
    }

    /**
     * activate analytical plugin
     */
    override fun start() {
        plugin.adapter?.fireResume()
    }


    /**
     * deactivate analytical plugin
     */
    override fun stop() {
        plugin.fireStop()
        plugin.removeAdapter()
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
}