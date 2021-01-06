package tv.mycujoo.mls.caster

import android.content.Context
import com.google.android.gms.cast.MediaSeekOptions

interface ICaster {
    fun initialize(context: Context, castListener: ICastListener): ISessionManagerListener

    fun loadRemoteMedia(params: CasterLoadRemoteMediaParams)
    fun play()
    fun pause()
    fun seek(mediaSeekOptions: MediaSeekOptions?)
    fun fastForward(amount: Long)


    fun rewind(amount: Long)
    fun onResume()
    fun onPause()
}