package tv.mycujoo.mls.caster

import android.content.Context
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

interface ICaster {
    fun initialize(context: Context, castListener: ICastListener): SessionManagerListener<CastSession>

    fun loadRemoteMedia(params: CasterLoadRemoteMediaParams)
    fun play()
    fun pause()
    fun seek(mediaSeekOptions: MediaSeekOptions?)
    fun fastForward(amount: Long)


    fun rewind(amount: Long)
    fun onResume()
    fun onPause()
}