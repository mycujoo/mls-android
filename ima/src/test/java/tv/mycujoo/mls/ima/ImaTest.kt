package tv.mycujoo.mls.ima

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertFailsWith

class ImaTest {

    private lateinit var ima: Ima

    @Mock
    lateinit var adsLoader: ImaAdsLoader

    @Mock
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory

    @Mock
    lateinit var player: Player

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        ima = Ima(adsLoader, SAMPLE_AD_TAG)
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

    companion object {
        private const val SAMPLE_AD_TAG = "sample_ad_tag"
    }
}