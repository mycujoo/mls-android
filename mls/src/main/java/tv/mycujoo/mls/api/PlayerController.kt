package tv.mycujoo.mls.api

import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

interface PlayerController {
    fun play()
    fun pause()

    fun config(videoPlayerConfig: VideoPlayerConfig)
}