package tv.mycujoo.mls.widgets

interface RemotePlayerControllerListener {
    fun onPlay()
    fun onPause()
    fun onSeekTo(newPosition: Long)
    fun onFastForward(amount: Long)
    fun onRewind(amount: Long)
}