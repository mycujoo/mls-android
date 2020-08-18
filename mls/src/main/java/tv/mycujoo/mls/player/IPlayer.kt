package tv.mycujoo.mls.player

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource

interface IPlayer {
    fun isReady(): Boolean
    fun create(mediaFactory: HlsMediaSource.Factory, exoPlayer: SimpleExoPlayer)

    // will be removed!
    fun getDirectInstance(): ExoPlayer?

    fun addListener(eventListener: Player.EventListener)

    fun seekTo(offset: Long)
    fun currentPosition(): Long
    fun duration(): Long
    fun isLive(): Boolean

    fun release()

    fun play(uriString: String)
    fun loadLastVideo()
}