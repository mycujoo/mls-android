package tv.mycujoo.mls.ima

import org.junit.Assert.assertEquals
import org.junit.Test

class ImaTest {
    @Test
    fun `ima instance returns ad-tag it was created with`() {
        val SAMPLE_AD_TAG = "sample_ad_tag"
        val ima = Ima(SAMPLE_AD_TAG)

        assertEquals(SAMPLE_AD_TAG, ima.getAdUnit())
    }
}