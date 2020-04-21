package tv.mycujoo.mls.model

import org.junit.Before
import org.junit.Test
import tv.mycujoo.mls.helper.TestDataHelper.Companion.getOverlayData
import kotlin.test.assertNotNull

internal class AnnotationBundleTest {

    @Before
    fun setUp() {
    }


    @Test
    internal fun `should have type`() {
        val annotationBundle = AnnotationBundle(AnnotationType.HIDE_OVERLAY, getOverlayData())

        assertNotNull(annotationBundle.type)
    }

    @Test
    internal fun `should have overlayData`() {
        val annotationBundle = AnnotationBundle(AnnotationType.SHOW_OVERLAY, OverlayData("Primary text"))

        assertNotNull(annotationBundle.overlayData)
    }
}