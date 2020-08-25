package tv.mycujoo.mls.api

import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

data class MLSConfiguration(
    val seekTolerance: Long = 1000L
) {
    val videoPlayerConfig: VideoPlayerConfig = defaultVideoPlayerConfig()
}

fun defaultVideoPlayerConfig(): VideoPlayerConfig {
    return VideoPlayerConfig(
        "#FFFFFF",
        "#000000",
        true,
        80F,
        true,
        liveViewers = true,
        eventInfoButton = true
    )
}
