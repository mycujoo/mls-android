package tv.mycujoo.cast

import com.google.android.gms.cast.framework.CastSession

interface ICastListener {
    fun onPlaybackLocationUpdated(isLocal: Boolean)
    fun onConnected(session: CastSession?)
    fun onDisconnecting(session: CastSession?)
    fun onDisconnected(session: CastSession?)
    fun onRemoteProgressUpdate(progressMs: Long, durationMs: Long)
    fun onRemotePlayStatusUpdate(isPlaying: Boolean, isBuffering: Boolean)
}