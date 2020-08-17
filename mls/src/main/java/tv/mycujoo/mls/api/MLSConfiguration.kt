package tv.mycujoo.mls.api

import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

data class MLSConfiguration(
    val VideoPlayerConfig: VideoPlayerConfig = defaultVideoPlayerConfig(),
    val accuracy: Long = 1000L
)

fun defaultVideoPlayerConfig(): VideoPlayerConfig {
    return VideoPlayerConfig(
        "#DC143C",
        "#FFFF00",
        true,
        80F,
        true,
        liveViewers = true,
        eventInfoButton = true
    )
}
