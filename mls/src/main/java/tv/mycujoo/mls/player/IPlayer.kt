package tv.mycujoo.mls.player

import android.os.Handler
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer

interface IPlayer {
    fun isReady(): Boolean
    fun create(
        mediaFactory: MediaFactory,
        exoPlayer: SimpleExoPlayer,
        handler: Handler,
        mediaOnLoadCompletedListener: MediaOnLoadCompletedListener
    )

    // will be removed!
    fun getDirectInstance(): ExoPlayer?
    fun getPlayer(): Player?

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

    fun pause()

    fun loadLastVideo()

    fun isWithinValidSegment(targetAbsoluteTime: Long): Boolean?
    fun dvrWindowSize(): Long
    fun dvrWindowStartTime(): Long
}