package tv.mycujoo.mls.ima

import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ImaTest {

    private lateinit var ima: Ima

    @Mock
    lateinit var defaultMediaSourceFactory: DefaultMediaSourceFactory

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val SAMPLE_AD_TAG = "sample_ad_tag"
        ima = Ima(SAMPLE_AD_TAG)
    }

    @Test
    fun `ima instance returns ad-tag it was created with`() {
        val SAMPLE_AD_TAG = "sample_ad_tag"
        val ima = Ima(SAMPLE_AD_TAG)

        assertEquals(SAMPLE_AD_TAG, ima.getAdUnit())
    }

    @Test
    fun setAdsLoaderProvider() {
        ima.setAdsLoaderProvider(defaultMediaSourceFactory)

        verify(defaultMediaSourceFactory).setAdsLoaderProvider(anyOrNull())
    }
}