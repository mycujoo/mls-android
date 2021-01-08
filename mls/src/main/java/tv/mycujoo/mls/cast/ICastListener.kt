package tv.mycujoo.mls.cast

interface ICastListener {
    fun onPlaybackLocationUpdated(isLocal: Boolean)
    fun onSessionStarted(session: ICasterSession?)
    fun onSessionStartFailed(session: ICasterSession?)
    fun onSessionResumed(session: ICasterSession?)
    fun onSessionResumeFailed(session: ICasterSession?)
    fun onSessionEnding(session: ICasterSession?)
    fun onSessionEnded(session: ICasterSession?)

    fun onRemoteProgressUpdate(progressMs: Long, durationMs: Long)
    fun onRemotePlayStatusUpdate(isPlaying: Boolean, isBuffering: Boolean)
    fun onRemoteLiveStatusUpdate(isLive: Boolean)
    fun onCastStateUpdated(showButton: Boolean)
}