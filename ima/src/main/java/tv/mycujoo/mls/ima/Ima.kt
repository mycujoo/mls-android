package tv.mycujoo.mls.ima

import android.content.Context
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.common.annotations.VisibleForTesting

class Ima(private val adUnit: String) : IIma {

    private lateinit var adsLoader: ImaAdsLoader

    @VisibleForTesting
    constructor(adsLoader: ImaAdsLoader, adUnit: String) : this(adUnit) {
        this.adsLoader = adsLoader
    }

    override fun getAdUnit(): String {
        return adUnit
    }

    override fun createAdsLoader(context: Context) {
        adsLoader =
            ImaAdsLoader.Builder(context)
                .setAdErrorListener { adErrorEvent: AdErrorEvent? ->
                }
                .setDebugModeEnabled(true)
                .build()
    }

    override fun setAdsLoaderProvider(defaultMediaSourceFactory: DefaultMediaSourceFactory) {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        val provider = DefaultMediaSourceFactory.AdsLoaderProvider { adsLoader }
        defaultMediaSourceFactory.setAdsLoaderProvider(provider)
    }
}