package tv.mycujoo.mls.ima

import android.content.Context
import android.net.Uri
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.common.annotations.VisibleForTesting
import java.net.URLEncoder
import java.util.*

class Ima(private val adUnit: String) : IIma {

    private lateinit var adsLoader: ImaAdsLoader
    private lateinit var adViewProvider: AdsLoader.AdViewProvider

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

    override fun setPlayer(player: Player) {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        adsLoader.setPlayer(player)
    }

    override fun setAdViewProvider(adViewProvider: AdsLoader.AdViewProvider) {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        this.adViewProvider = adViewProvider
    }

    override fun createMediaSource(
        defaultMediaSourceFactory: DefaultMediaSourceFactory,
        hlsMediaSource: MediaSource,
        imaCustomParams: ImaCustomParams
    ): MediaSource {
        val adsMediaSource = AdsMediaSource(
            hlsMediaSource,
            DataSpec(getAdTagUri(imaCustomParams)),
            defaultMediaSourceFactory,
            adsLoader,
            adViewProvider
        )

        return adsMediaSource
    }

    private fun getAdTagUri(imaCustomParams: ImaCustomParams): Uri {
        fun getEncodedCustomParams(imaCustomParams: ImaCustomParams): String {
            if (imaCustomParams.isEmpty()) {
                return "deployment%3Ddevsite%26sample_ct%3Dlinear"
            } else {
                val stringBuilder = StringBuilder()
                    .append("deployment=devsite&sample_ct=linear")
                imaCustomParams.writeValues(stringBuilder)
                return URLEncoder.encode(stringBuilder.toString(), "utf-8")
            }
        }

        val stringBuilder = StringBuilder()
            .append("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/")
            .append(adUnit)
            .append("&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1")
            .append("&cust_params=".plus(getEncodedCustomParams(imaCustomParams)))
            .append("&correlator=".plus(Date().time))

        return Uri.parse(stringBuilder.toString())
    }
}