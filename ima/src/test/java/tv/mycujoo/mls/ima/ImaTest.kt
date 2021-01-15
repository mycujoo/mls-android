package tv.mycujoo.mls.ima

import com.google.ads.interactivemedia.v3.api.Ad
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ads.AdsLoader
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertFailsWith

class ImaTest {

    private lateinit var ima: Ima

    @Mock
    lateinit var builder: ImaAdsLoader.Builder

    @Mock
    lateinit var adsLoader: ImaAdsLoader

    @Mock
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory

    @Mock
    lateinit var viewProvider: AdsLoader.AdViewProvider

    @Mock
    lateinit var player: Player

    @Mock
    lateinit var listener: ImaEventListener
    private lateinit var adEventListener: AdEvent.AdEventListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(builder.setAdErrorListener(any())).thenReturn(builder)
        whenever(builder.setAdEventListener(any())).thenAnswer {
            adEventListener = it.arguments[0] as AdEvent.AdEventListener
            (builder)
        }
        whenever(builder.setDebugModeEnabled(any())).thenReturn(builder)
        whenever(builder.build()).thenReturn(adsLoader)
        ima = Ima(builder, listener, SAMPLE_AD_TAG)
    }

    @Test
    fun `ima instance returns ad-tag it was created with`() {
        val ima = Ima(SAMPLE_AD_TAG)

        assertEquals(SAMPLE_AD_TAG, ima.getAdUnit())
    }

    @Test
    fun setAdsLoaderProvider() {
        ima.setAdsLoaderProvider(defaultMediaSourceFactory)

        verify(defaultMediaSourceFactory).setAdsLoaderProvider(anyOrNull())
    }

    @Test
    fun `trying to set AdsLoaderProvider on a non-created-adsLoader Ima throws IllegalStateException`() {
        ima = Ima(SAMPLE_AD_TAG)
        // Not calling createAdsLoader() !

        assertFailsWith(IllegalStateException::class) {
            ima.setAdsLoaderProvider(
                defaultMediaSourceFactory
            )
        }
    }

    @Test
    fun setPlayer() {
        ima.setPlayer(player)

        verify(adsLoader).setPlayer(player)
    }

    @Test
    fun `trying to set player on a non-created-adsLoader Ima throws IllegalStateException`() {
        ima = Ima(SAMPLE_AD_TAG)
        // Not calling createAdsLoader() !

        assertFailsWith(IllegalStateException::class) {
            ima.setPlayer(
                player
            )
        }
    }

    @Test
    fun `trying to set ViewProvider on a non-created-adsLoader Ima throws IllegalStateException`() {
        ima = Ima(SAMPLE_AD_TAG)
        // Not calling createAdsLoader() !

        assertFailsWith(IllegalStateException::class) {
            ima.setAdViewProvider(
                viewProvider
            )
        }
    }

    @Test
    fun `started-ad calls listener's onStarted`() {
        val started = getAdEvent(AdEvent.AdEventType.STARTED)
        adEventListener.onAdEvent(started)


        verify(listener).onAdStarted()
    }

    @Test
    fun `paused-ad calls listener's onPaused`() {
        val paused = getAdEvent(AdEvent.AdEventType.PAUSED)
        adEventListener.onAdEvent(paused)


        verify(listener).onAdPaused()
    }

    @Test
    fun `resumed-ad calls listener's onResumed`() {
        val resumed = getAdEvent(AdEvent.AdEventType.RESUMED)
        adEventListener.onAdEvent(resumed)


        verify(listener).onAdResumed()
    }

    @Test
    fun `completed-ad calls listener's onAdCompleted`() {
        val completed = getAdEvent(AdEvent.AdEventType.COMPLETED)
        adEventListener.onAdEvent(completed)


        verify(listener).onAdCompleted()
    }


    @Test
    fun `destroying adsLoader on a non-created-adsLoader Ima throws IllegalStateException`() {
        ima = Ima(SAMPLE_AD_TAG)
        // Not calling createAdsLoader() !


        assertFailsWith(IllegalStateException::class) {
            ima.onDestroy()
        }
    }

    @Test
    fun `release adsLoader in onDestroy`() {
        ima.onDestroy()


        verify(adsLoader).release()
    }

    companion object {
        private const val SAMPLE_AD_TAG = "sample_ad_tag"

        private fun getAdEvent(type: AdEvent.AdEventType): AdEvent {
            return object : AdEvent {
                override fun getType(): AdEvent.AdEventType {
                    return type
                }

                override fun getAd(): Ad {
                    TODO("Not yet implemented")
                }

                override fun getAdData(): MutableMap<String, String> {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}