package tv.mycujoo.mcls.analytic

import android.app.Activity
import androidx.annotation.VisibleForTesting
import com.google.android.exoplayer2.ExoPlayer
import com.npaw.youbora.lib6.YouboraLog
import com.npaw.youbora.lib6.exoplayer2.Exoplayer2Adapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.mcls.enum.DeviceType
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import javax.inject.Inject

/**
 * Integration with Youbora, the analytical tool.
 * @param logger to log events for developers.
 * @param userPreferencesUtils to get user specific params (This is a singleton)
 */
class YouboraClient @Inject constructor(
    private val logger: Logger,
    private val userPreferencesUtils: UserPreferencesUtils
) : AnalyticsClient {

    private var plugin: Plugin? = null
    private var videoAnalyticsCustomData: VideoAnalyticsCustomData? = null

    /**
     * Only AnalyticsClient should know about the implementation of the analytics server and libs
     * This ensures only this class knows about youbora
     */
    fun setYouboraPlugin(
        activity: Activity,
        exoPlayer: ExoPlayer,
        accountCode: String,
        deviceType: DeviceType,
        videoAnalyticsCustomData: VideoAnalyticsCustomData?,
    ) {
        val youboraOptions = Options()
        youboraOptions.accountCode = accountCode
        youboraOptions.isAutoDetectBackground = true

        when (deviceType) {
            DeviceType.ANDROID_TV -> {
                youboraOptions.deviceCode = "AndroidTV"
            }
            DeviceType.FIRE_TV -> {
                youboraOptions.deviceCode = "FireTV"
            }
            DeviceType.ANDROID -> {
                youboraOptions.deviceCode = "Android"
            }
        }

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

        this.videoAnalyticsCustomData = videoAnalyticsCustomData
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
        savedPlugin.options.username = userPreferencesUtils.getPseudoUserId()
        savedPlugin.options.contentTitle = event.title
        savedPlugin.options.contentResource = event.streams.firstOrNull()?.toString()
        savedPlugin.options.contentIsLive = live

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
        plugin?.adapter?.fireResume()
    }


    /**
     * deactivate analytical plugin
     */
    override fun stop() {
        plugin?.fireStop()
        plugin?.removeAdapter()
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