package tv.mycujoo.mls.caster

interface ICastListener {
    fun onPlaybackLocationUpdated(isLocal: Boolean)
    fun onConnected(session: ICasterSession?)
    fun onDisconnecting(session: ICasterSession?)
    fun onDisconnected(session: ICasterSession?)
    fun onRemoteProgressUpdate(progressMs: Long, durationMs: Long)
    fun onRemotePlayStatusUpdate(isPlaying: Boolean, isBuffering: Boolean)
    fun onRemoteLiveStatusUpdate(isLive: Boolean)
    fun onCastStateUpdated(showButton: Boolean)
}