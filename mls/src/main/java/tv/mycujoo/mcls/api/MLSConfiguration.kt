package tv.mycujoo.mcls.api

import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.LogLevel

/**
 * configuration for MLS component which set behavioural and visual settings of MLS SDK
 * @param seekTolerance how much should a Time-line action be seek-able back and forth. Defaults to 1ms.
 * @param videoPlayerConfig configs related to video-player itself. i.e. show auto play vs hide
 * @param logLevel level of logging that SDK should output. this ranges from MINIMAL to VERBOSE
 *
 * @see VideoPlayerConfig
 * @see LogLevel
 */
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
