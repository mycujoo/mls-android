package tv.mycujoo.cast

import com.google.android.gms.cast.framework.CastSession

interface ICastListener {
    fun onPlaybackLocationUpdated(isLocal: Boolean)
    fun onConnected(session: CastSession?)
    fun onDisconnecting(session: CastSession?)
    fun onDisconnected(session: CastSession?)
}