package tv.mycujoo.mls.api

import tv.mycujoo.mls.entity.msc.VideoPlayerConfig

interface PlayerController {
    fun play()
    fun pause()
    fun seekTo(position: Int)
    fun currentTime(): Int
    fun optimisticCurrentTime(): Int
    fun currentDuration(): Int
    fun isMuted() : Boolean
    fun mute()

    fun showEventInfoOverlay()
    fun hideEventInfoOverlay()

    fun config(videoPlayerConfig: VideoPlayerConfig)
}