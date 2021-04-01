package tv.mycujoo.mcls.api

import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.LogLevel

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
        showEventInfoButton = true
    )
}
