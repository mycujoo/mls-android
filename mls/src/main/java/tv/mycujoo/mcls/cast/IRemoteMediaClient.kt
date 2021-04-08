package tv.mycujoo.mcls.cast

interface IRemoteMediaClient {
    fun isPlaying(): Boolean
    fun approximateStreamPosition(): Long
}