package tv.mycujoo.mls.model

import org.junit.Test
import kotlin.test.assertNotNull


class OverlayDataTest {

    @Test
    fun `should have primary text`() {
        val overlayData = OverlayData("Primary text")
        assertNotNull(overlayData.primaryText)
    }
}