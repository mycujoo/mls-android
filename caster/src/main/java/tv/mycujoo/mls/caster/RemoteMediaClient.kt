package tv.mycujoo.mls.caster

import com.google.android.gms.cast.framework.media.RemoteMediaClient

class RemoteMediaClient(private val remoteMediaClient: RemoteMediaClient) : IRemoteMediaClient {
    override fun isPlaying(): Boolean {
        return remoteMediaClient.isPlaying
    }

    override fun approximateStreamPosition(): Long {
        return remoteMediaClient.approximateStreamPosition
    }

}