package tv.mycujoo.mls.player

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource

interface IPlayer {
    fun isReady(): Boolean
    fun create(
        mediaFactory: HlsMediaSource.Factory,
        exoPlayer: SimpleExoPlayer,
        mediaOnLoadCompletedListener: MediaOnLoadCompletedListener
    )

    // will be removed!
    fun getDirectInstance(): ExoPlayer?

    fun addListener(eventListener: Player.EventListener)

    fun seekTo(offset: Long)
    fun currentPosition(): Long
    fun currentAbsoluteTime(): Long
    fun duration(): Long
    fun isLive(): Boolean
    fun isPlaying(): Boolean

    fun release()

    fun play(uriString: String, dvrWindowSize: Long = Long.MAX_VALUE, autoPlay: Boolean)
    fun play(
        uriString: String,
        dvrWindowSize: Long = Long.MAX_VALUE,
        licenseUrl: String,
        autoPlay: Boolean
    )

    fun loadLastVideo()

    fun isWithinValidSegment(targetAbsoluteTime: Long): Boolean?
    fun dvrWindowSize(): Long
}