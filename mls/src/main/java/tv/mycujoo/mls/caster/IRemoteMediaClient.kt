package tv.mycujoo.mls.caster

interface IRemoteMediaClient {
    fun isPlaying(): Boolean
    fun approximateStreamPosition(): Long
}