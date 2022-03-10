package tv.mycujoo.mcls.ima

import android.content.Context
import android.net.Uri
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType.*
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.AdViewProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.common.annotations.VisibleForTesting
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.mcls.enum.C
import java.net.URLEncoder
import java.util.*

/**
 * MLS IMA integration to use Google IMA
 * @param adUnit adUnit which is provided by Google IMA panel
 * @param liveAdUnit adUnit for live events, provided by Google IMA panel
 * @param paramProvider custom parameter to log through IMA
 * @param debugMode debug/release mode the SDK is running. Caused to use debug adUnit
 */
class Ima(
    private val adUnit: String,
    private val liveAdUnit: String? = null,
    private val paramProvider: IParamProvider? = null,
    private val listener: ImaEventListener? = null,
    private val debugMode: Boolean = false
) : IIma {

    private lateinit var adsLoader: ImaAdsLoader
    private lateinit var adViewProvider: AdViewProvider

    @VisibleForTesting
    constructor(
        builder: ImaAdsLoader.Builder,
        listener: ImaEventListener,
        adUnit: String,
        liveAdUnit: String
    ) : this(adUnit, liveAdUnit, null, null, true) {
        adsLoader = createAdsLoader(builder, listener)
    }

    init {
        if (adUnit[0] != '/') {
            throw IllegalArgumentException(C.AD_UNIT_MUST_START_WITH_SLASH_IN_MLS_BUILDER_MESSAGE)
        }
    }

    /**
     * AdUnit to feed Google IMA
     */
    override fun getAdUnit(): String {
        return adUnit
    }

    /**
     * Create ImaAdsLoader
     * @param context app/activity context
     */
    override fun createAdsLoader(context: Context) {
        val builder = ImaAdsLoader.Builder(context)
        adsLoader = createAdsLoader(builder, listener)
    }

    /**
     * Internal use: create ImaAdsLoader
     * @param builder builder with access to Context
     * @param listener callback for ad lifecycle
     * @see ImaEventListener
     */
    private fun createAdsLoader(
        builder: ImaAdsLoader.Builder,
        listener: ImaEventListener? = null
    ): ImaAdsLoader {
        return builder
            .setAdEventListener { adEvent ->
                when (adEvent.type) {
                    STARTED -> {
                        listener?.onAdStarted()
                    }
                    PAUSED -> {
                        listener?.onAdPaused()
                    }
                    RESUMED -> {
                        listener?.onAdResumed()
                    }
                    COMPLETED -> {
                        listener?.onAdCompleted()
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
            .setDebugModeEnabled(debugMode)
            .build()
    }

    /**
     * Set Ima AdsLoaderProvider to MediaSourceFactory
     * @param defaultMediaSourceFactory MediaSourceFactory to create media item for exo-player
     */
    override fun setAdsLoaderProvider(defaultMediaSourceFactory: DefaultMediaSourceFactory) {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        val provider = AdsLoader.Provider { adsLoader }
        defaultMediaSourceFactory.setAdsLoaderProvider(provider)
    }

    /**
     * Set player to AdsLoader.
     * Must happen before using the IMA, and after AdsLoader is initialized
     * @param player exoplayer mediaplyer interface
     * @see Player
     */
    override fun setPlayer(player: Player) {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        adsLoader.setPlayer(player)
    }

    /**
     *
     */
    override fun setAdViewProvider(adViewProvider: AdViewProvider) {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        this.adViewProvider = adViewProvider
    }

    /**
     * Create Media Source when IMA is active.
     * Must be used when IMA integration is active.
     * Exoplayer uses MediaSource to load content.
     * @param defaultMediaSourceFactory default factory for creating MediaSource
     * @param hlsMediaSource source for defining MediaSource
     * @param imaCustomParams MLS custom parameter for event
     * @return MediaSource
     */
    override fun createMediaSource(
        defaultMediaSourceFactory: DefaultMediaSourceFactory,
        hlsMediaSource: MediaSource,
        imaCustomParams: ImaCustomParams
    ): MediaSource {
        return AdsMediaSource(
            hlsMediaSource,
            DataSpec(getAdTagUri(imaCustomParams, paramProvider?.params() ?: emptyMap())),
            listOf(this.adUnit, this.liveAdUnit),
            defaultMediaSourceFactory,
            adsLoader,
            adViewProvider
        )
    }

    /**
     * Create Uri for Ad including given parameters
     * @param imaCustomParams Event related parameters
     * @param params user defined parameters
     */
    private fun getAdTagUri(imaCustomParams: ImaCustomParams, params: Map<String, String>): Uri {
        fun getAdUnitBasedOnEventStatus(eventStatus: EventStatus?): String {
            return when (eventStatus) {
                EventStatus.EVENT_STATUS_STARTED -> {
                    liveAdUnit ?: adUnit
                }
                else -> {
                    adUnit
                }
            }
        }

        fun getEncodedCustomParams(imaCustomParams: ImaCustomParams): String {
            return if (imaCustomParams.isEmpty()) {
                if (debugMode) {
                    "deployment%3Ddevsite%26sample_ct%3Dlinear"
                } else {
                    ""
                }
            } else {
                val stringBuilder = StringBuilder()
                if (debugMode) {
                    stringBuilder.append("deployment=devsite&sample_ct=linear")
                }
                imaCustomParams.writeValues(stringBuilder, params)
                URLEncoder.encode(stringBuilder.toString(), "utf-8")
            }
        }


        val stringBuilder = StringBuilder()
            .append("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=")
            .append(getAdUnitBasedOnEventStatus(imaCustomParams.eventStatus))
            .append("&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1")
            .append("&cust_params=".plus(getEncodedCustomParams(imaCustomParams)))
            .append("&correlator=".plus(Date().time))

        return Uri.parse(stringBuilder.toString())
    }


    /**
     * Remove exoplayer from AdsLoader
     * Must be called when hosting app is going to background
     */
    override fun onStop() {
        adsLoader.setPlayer(null)
    }

    /**
     * Destroy AdsLoader
     * Must be called on app/SDK destroy to release resources
     */
    override fun onDestroy() {
        if (this::adsLoader.isInitialized.not()) {
            throw IllegalStateException()
        }
        adsLoader.release()
    }
}