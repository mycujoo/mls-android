package tv.mycujoo.mls.player

import android.os.Handler
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mls.ima.IIma

interface IPlayer {
    fun isReady(): Boolean
    fun create(
        ima: IIma?,
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

    fun play(drmMediaData: MediaDatum.DRMMediaData)
    fun play(mediaData: MediaDatum.MediaData)

    fun play()
    fun pause()

    fun loadLastVideo()

    fun isWithinValidSegment(targetAbsoluteTime: Long): Boolean?
    fun dvrWindowSize(): Long
    fun dvrWindowStartTime(): Long
}