package tv.mycujoo.mls.caster

import com.google.android.gms.cast.framework.media.RemoteMediaClient

interface ICaster {
    fun initialize(castProvider: ICastContextProvider, castListener: ICastListener)
    fun onResume()
    fun onPause()
    fun getRemoteMediaClient(): RemoteMediaClient?

}
