package tv.mycujoo.mls.caster

import com.google.android.gms.cast.framework.CastSession

class CasterSession : ICasterSession {
    var castSession: CastSession? = null
    override fun getRemoteMediaClient(): IRemoteMediaClient? {
        castSession?.let {
            return RemoteMediaClient(it.remoteMediaClient)
        }
        return null
    }
}