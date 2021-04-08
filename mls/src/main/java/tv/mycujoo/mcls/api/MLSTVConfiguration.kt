package tv.mycujoo.mcls.api

import tv.mycujoo.mcls.entity.msc.TVVideoPlayerConfig
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.LogLevel

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
