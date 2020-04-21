package tv.mycujoo.mls.api

interface PlayerEvents {
    fun onLoadingChanged(loading: Boolean)
    fun onPlayerError(e: Exception)
    fun onIsPlayingChanged(playing: Boolean)
    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)

}