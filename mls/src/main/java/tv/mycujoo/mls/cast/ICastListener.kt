package tv.mycujoo.mls.cast

interface ICastListener {
    fun onPlaybackLocationUpdated(isLocal: Boolean)
    fun onConnected(session: ICasterSession?)
    fun onDisconnecting(session: ICasterSession?)
    fun onDisconnected(session: ICasterSession?)
    fun onSessionResumed(session: ICasterSession?)

    fun onRemoteProgressUpdate(progressMs: Long, durationMs: Long)
    fun onRemotePlayStatusUpdate(isPlaying: Boolean, isBuffering: Boolean)
    fun onRemoteLiveStatusUpdate(isLive: Boolean)
    fun onCastStateUpdated(showButton: Boolean)
}