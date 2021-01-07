package tv.mycujoo.mls.cast

interface IRemoteMediaClient {
    fun isPlaying(): Boolean
    fun approximateStreamPosition(): Long
}