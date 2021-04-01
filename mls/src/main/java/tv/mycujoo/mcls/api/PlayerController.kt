package tv.mycujoo.mcls.api

import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig

interface PlayerController {
    fun play()
    fun pause()
    fun seekTo(position: Int)
    fun currentTime(): Int
    fun optimisticCurrentTime(): Int
    fun currentDuration(): Int
    fun isMuted() : Boolean
    fun mute()
    fun isPlayingAd(): Boolean

    fun showEventInfoOverlay()
    fun hideEventInfoOverlay()

    fun config(videoPlayerConfig: VideoPlayerConfig)
}