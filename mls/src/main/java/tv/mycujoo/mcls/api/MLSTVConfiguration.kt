package tv.mycujoo.mcls.api

import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.LogLevel

/**
 * configuration for MLS-TV component which set behavioural and visual settings of SDK
 * @param seekTolerance how much should a Time-line action be seek-able back and forth. Defaults to 1ms.
 * @param videoPlayerConfig configs related to video-player itself. i.e. show auto play vs hide
 * @param logLevel level of logging that SDK should output. this ranges from MINIMAL to VERBOSE
 *
 * @see TVVideoPlayerConfig
 * @see LogLevel
 */
data class MLSTVConfiguration(
    val seekTolerance: Long = ONE_SECOND_IN_MS,
    val videoPlayerConfig: TVVideoPlayerConfig = defaultTVVideoPlayerConfig(),
    val logLevel: LogLevel = LogLevel.MINIMAL
)

fun defaultTVVideoPlayerConfig(): TVVideoPlayerConfig {
    return TVVideoPlayerConfig(
        primaryColor = "#FFFFFF",
        secondaryColor = "#000000",
        autoPlay = true,
        showBackForwardsButtons = true,
        showSeekBar = true,
        showTimers = true,
        showLiveViewers = true
    )
}
