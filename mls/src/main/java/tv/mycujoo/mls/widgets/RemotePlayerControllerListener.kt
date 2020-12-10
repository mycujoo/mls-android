package tv.mycujoo.mls.widgets

interface RemotePlayerControllerListener {
    fun onSeekTo(newPosition: Long)
    fun onFastForward(amount: Long)
    fun onRewind(amount: Long)
}