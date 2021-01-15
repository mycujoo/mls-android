package tv.mycujoo.mls.ima

import android.content.Context
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsLoader

interface IIma {
    fun getAdUnit(): String
    fun createAdsLoader(context: Context)
    fun setAdsLoaderProvider(defaultMediaSourceFactory: DefaultMediaSourceFactory)
    fun setPlayer(player: Player)
    fun setAdViewProvider(adViewProvider: AdsLoader.AdViewProvider)
    fun createMediaSource(
        defaultMediaSourceFactory: DefaultMediaSourceFactory,
        hlsMediaSource: MediaSource,
        imaCustomParams: ImaCustomParams
    ): MediaSource

    fun onDestroy()
}