package tv.mycujoo.mls.caster

import android.content.Context
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaSeekOptions

interface ICaster {
    fun initialize(context: Context?, castListener: ICastListener)

    fun loadRemoteMedia(mediaInfo: MediaInfo, mediaLoadOptions: MediaLoadOptions)
    fun play()
    fun pause()
    fun seek(mediaSeekOptions: MediaSeekOptions?)
    fun fastForward(amount: Long)


    fun rewind(amount: Long)
    fun onResume()
    fun onPause()
}