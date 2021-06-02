package tv.mycujoo.mcls.cast

import android.content.Context

/**
 * Contract for integrating Google Cast in to MLS SDK
 */
interface ICast {
    fun initialize(context: Context, castListener: ICastListener): ISessionManagerListener

    fun loadRemoteMedia(params: CasterLoadRemoteMediaParams)
    fun play()
    fun pause()
    fun seekTo(position: Long)
    fun fastForward(amount: Long)
    fun rewind(amount: Long)
    fun currentPosition(): Long?

    fun onResume()
    fun onPause()

}