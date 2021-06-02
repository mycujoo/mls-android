package tv.mycujoo.mcls.cast

import com.google.android.gms.cast.framework.media.RemoteMediaClient

/**
 * Provides abstraction over Android GMS package remote media client.
 * Information like player state and position can be provided here.
 * Implementation of IRemoteMediaClient.
 */
class RemoteMediaClient(private val remoteMediaClient: RemoteMediaClient) : IRemoteMediaClient {
    /**
     * Remote media client current state
     * @return true is playing, false otherwise
     */
    override fun isPlaying(): Boolean {
        return remoteMediaClient.isPlaying
    }

    /**
     * Remote media client current position
     * @return current approximate position of remote media client
     */
    override fun approximateStreamPosition(): Long {
        return remoteMediaClient.approximateStreamPosition
    }

}