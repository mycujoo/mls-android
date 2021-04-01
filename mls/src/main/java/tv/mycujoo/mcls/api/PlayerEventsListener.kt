package tv.mycujoo.mcls.api

interface PlayerEventsListener {

    fun onIsPlayingChanged(playing: Boolean)
    fun onPlayerStateChanged(playbackState: Int)

}

