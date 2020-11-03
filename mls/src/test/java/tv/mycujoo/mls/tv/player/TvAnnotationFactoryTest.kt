package tv.mycujoo.mls.tv.player

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData

class TvAnnotationFactoryTest {


    private lateinit var tvAnnotationFactory: TvAnnotationFactory

    @Mock
    lateinit var tvAnnotationListener: TvAnnotationListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tvAnnotationFactory = TvAnnotationFactory(tvAnnotationListener)
    }

    @Test
    fun `empty list, should not add or remove anything`() {
        tvAnnotationFactory.setAnnotations(emptyList())


        tvAnnotationFactory.build(0L, isPlaying = true, interrupted = false)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any())
    }

    @Test
    fun `add overlay`() {
        val parsedOverlayRelatedData = ParsedOverlayRelatedData(
            "id_0",
            "",
            -1L,
            PositionGuide(),
            Pair(-1F, -1F),
            AnimationType.NONE,
            -1L,
            AnimationType.UNSPECIFIED,
            -1L,
            emptyList()
        )
        val actionObject = ActionObject(
            "id_0",
            ActionType.SHOW_OVERLAY,
            500L,
            parsedOverlayRelatedData,
            null,
            null
        )
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(0L, isPlaying = true, interrupted = false)


        Mockito.verify(tvAnnotationListener).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any())
    }

    @Test
    fun `remove overlay`() {
        val parsedOverlayRelatedData = ParsedOverlayRelatedData(
            "id_0",
            "",
            3000L,
            PositionGuide(),
            Pair(-1F, -1F),
            AnimationType.NONE,
            -1L,
            AnimationType.NONE,
            -1L,
            emptyList()
        )
        val actionObject = ActionObject(
            "id_0",
            ActionType.SHOW_OVERLAY,
            500L,
            parsedOverlayRelatedData,
            null,
            null
        )
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(3500L, isPlaying = true, interrupted = false)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener).removeOverlay(any())
    }
    @Test
    fun `do nothing`() {
        val parsedOverlayRelatedData = ParsedOverlayRelatedData(
            "id_0",
            "",
            3000L,
            PositionGuide(),
            Pair(-1F, -1F),
            AnimationType.NONE,
            -1L,
            AnimationType.NONE,
            -1L,
            emptyList()
        )
        val actionObject = ActionObject(
            "id_0",
            ActionType.SHOW_OVERLAY,
            500L,
            parsedOverlayRelatedData,
            null,
            null
        )
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(2500L, isPlaying = true, interrupted = false)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any())
    }
}