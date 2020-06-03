package tv.mycujoo.mls.api

interface PlayerEvents {

    fun onPlay()
    fun onPause()
    fun onEnd()

/*      UNCOMMENT for more events

    fun onLoadingChanged(loading: Boolean)
    fun onPlayerError(e: Exception)
    fun onIsPlayingChanged(playing: Boolean)
    fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)*/

}