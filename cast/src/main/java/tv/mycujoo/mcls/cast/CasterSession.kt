package tv.mycujoo.mcls.cast

import com.google.android.gms.cast.framework.CastSession

class CasterSession : ICasterSession {
    var castSession: CastSession? = null
    override fun getRemoteMediaClient(): IRemoteMediaClient? {
        castSession?.remoteMediaClient?.let {
            return RemoteMediaClient(it)
        }
        return null
    }
}