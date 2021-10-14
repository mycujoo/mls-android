package tv.mycujoo.mcls.helper

import android.graphics.Typeface
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import tv.mycujoo.mcls.helper.SVGAssetResolver.Companion.ROBOTO_MONO_BOLD
import tv.mycujoo.mcls.helper.SVGAssetResolver.Companion.ROBOTO_MONO_BOLD_FILE_NAME
import tv.mycujoo.mcls.helper.SVGAssetResolver.Companion.ROBOTO_MONO_REGULAR
import tv.mycujoo.mcls.helper.SVGAssetResolver.Companion.ROBOTO_MONO_REGULAR_FILE_NAME
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SVGAssetResolverTest {

    private lateinit var svgAssetResolver: SVGAssetResolver

    @Mock
    lateinit var typeFaceFactory: ITypeFaceFactory

    @Mock
    lateinit var robotoMonoBoldTypeface: Typeface

    @Mock
    lateinit var robotoMonoRegularTypeface: Typeface

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(typeFaceFactory.createFromAsset(ROBOTO_MONO_BOLD_FILE_NAME)).thenReturn(
            robotoMonoBoldTypeface
        )
        whenever(typeFaceFactory.createFromAsset(ROBOTO_MONO_REGULAR_FILE_NAME)).thenReturn(
            robotoMonoRegularTypeface
        )

        svgAssetResolver = SVGAssetResolver(typeFaceFactory)
    }

    @Test
    fun `create Roboto-Mono Bold Typeface test`() {
        val typeFace = svgAssetResolver.resolveFont(ROBOTO_MONO_BOLD, 14, null)

        assertEquals(robotoMonoBoldTypeface, typeFace)
    }


    @Test
    fun `create Roboto-Mono Regular Typeface test`() {
        val typeFace = svgAssetResolver.resolveFont(ROBOTO_MONO_REGULAR, 14, null)

        assertEquals(robotoMonoRegularTypeface, typeFace)
    }

    @Test
    fun `invalid font name should return null`() {
        var typeFace = svgAssetResolver.resolveFont("", 14, null)
        assertNull(typeFace)
        typeFace = svgAssetResolver.resolveFont(null, 14, null)
        assertNull(typeFace)
    }
}