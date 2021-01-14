package tv.mycujoo.mls.ima

import android.content.Context
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

interface IIma {
    fun getAdUnit(): String
    fun createAdsLoader(context: Context)
    fun setAdsLoaderProvider(defaultMediaSourceFactory: DefaultMediaSourceFactory)
    fun setPlayer(player: Player)

}