package tv.mycujoo.mls.ima

import com.google.android.exoplayer2.source.DefaultMediaSourceFactory

class Ima(private val adUnit: String) : IIma {

    override fun getAdUnit(): String {
        return adUnit
    }

    override fun setAdsLoaderProvider(defaultMediaSourceFactory: DefaultMediaSourceFactory) {
        defaultMediaSourceFactory.setAdsLoaderProvider(null)
    }
}