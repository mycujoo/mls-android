package tv.mycujoo.mls.ima

import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

interface IIma {
    fun getAdUnit(): String
    fun setAdsLoaderProvider(defaultMediaSourceFactory: DefaultMediaSourceFactory)
}