package tv.mycujoo.mls.api

interface PlayerEventsListener {

    fun onIsPlayingChanged(playing: Boolean)
    fun onPlayerStateChanged(playbackState: Int)

}

