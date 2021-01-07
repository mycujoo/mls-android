package tv.mycujoo.mls.cast

import android.content.Context

interface ICaster {
    fun initialize(context: Context, castListener: ICastListener): ISessionManagerListener

    fun loadRemoteMedia(params: CasterLoadRemoteMediaParams)
    fun play()
    fun pause()
    fun seekTo(position: Long)
    fun fastForward(amount: Long)


    fun rewind(amount: Long)
    fun onResume()
    fun onPause()
}