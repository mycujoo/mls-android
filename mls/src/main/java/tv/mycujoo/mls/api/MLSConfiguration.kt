package tv.mycujoo.mls.api

import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.enum.LogLevel

data class MLSConfiguration(
    val seekTolerance: Long = ONE_SECOND_IN_MS,
    val videoPlayerConfig: VideoPlayerConfig = defaultVideoPlayerConfig(),
    val logLevel: LogLevel = LogLevel.MINIMAL
)

fun defaultVideoPlayerConfig(): VideoPlayerConfig {
    return VideoPlayerConfig(
        primaryColor = "#FFFFFF",
        secondaryColor = "#000000",
        autoPlay = true,
        enableControls = true,
        showPlayPauseButtons = true,
        showBackForwardsButtons = true,
        showSeekBar = true,
        showTimers = true,
        showFullScreenButton = false,
        showLiveViewers = true,
        showEventInfoButton = true,
        showCastButton = false
    )
}
