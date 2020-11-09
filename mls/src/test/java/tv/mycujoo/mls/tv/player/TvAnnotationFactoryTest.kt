package tv.mycujoo.mls.tv.player

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData
import tv.mycujoo.mls.TestData
import tv.mycujoo.mls.toActionObject

@ExperimentalStdlibApi
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
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
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
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
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
        Mockito.verify(tvAnnotationListener).removeOverlay(any<OverlayEntity>())
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
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
    }

    @Test
    fun `add or update lingering intro overlay`() {

        val actionObject =
            TestData.sampleEntityWithIntroAnimation(AnimationType.SLIDE_FROM_LEFT)
                .toActionObject(500L, -1L)
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(600L, isPlaying = true, interrupted = true)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener, never())
            .addOrUpdateLingeringOutroOverlay(any(), any(), any())
        Mockito.verify(tvAnnotationListener)
            .addOrUpdateLingeringIntroOverlay(any<OverlayEntity>(), any(), any())
    }

    @Test
    fun `add or update lingering outro overlay`() {
        val actionObject =
            TestData.sampleEntityWithOutroAnimation(AnimationType.SLIDE_TO_LEFT)
                .toActionObject(500L, 3000L)
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(3600L, isPlaying = true, interrupted = true)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener, never())
            .addOrUpdateLingeringIntroOverlay(any(), any(), any())
        Mockito.verify(tvAnnotationListener).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    @Test
    fun `add or update lingering midway overlay`() {
        val actionObject =
            TestData.sampleEntityWithOutroAnimation(AnimationType.SLIDE_TO_LEFT)
                .toActionObject(500L, 3000L)
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(2600L, isPlaying = true, interrupted = true)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener, never())
            .addOrUpdateLingeringIntroOverlay(any(), any(), any())
        Mockito.verify(tvAnnotationListener, never())
            .addOrUpdateLingeringOutroOverlay(any(), any(), any())
        Mockito.verify(tvAnnotationListener).addOrUpdateLingeringMidwayOverlay(any())
    }

    @Test
    fun `remove lingering overlay`() {
        val actionObject =
            TestData.sampleEntityWithOutroAnimation(AnimationType.SLIDE_TO_LEFT)
                .toActionObject(500L, 3000L)
        tvAnnotationFactory.setAnnotations(listOf(actionObject))


        tvAnnotationFactory.build(5000L, isPlaying = true, interrupted = true)


        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener, never())
            .addOrUpdateLingeringIntroOverlay(any(), any(), any())
        Mockito.verify(tvAnnotationListener, never())
            .addOrUpdateLingeringOutroOverlay(any(), any(), any())
        Mockito.verify(tvAnnotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        Mockito.verify(tvAnnotationListener).removeLingeringOverlay(any())
    }

    @Test
    fun `hide overlay in range`() {
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
        val hideActionObject = ActionObject(
            "id_0",
            ActionType.HIDE_OVERLAY,
            5000L,
            parsedOverlayRelatedData,
            null,
            null
        )
        tvAnnotationFactory.setAnnotations(listOf(hideActionObject))

        tvAnnotationFactory.build(4500L, isPlaying = true, interrupted = false)

        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener).removeOverlay(any<HideOverlayActionEntity>())
    }

    @Test
    fun `hide overlay out of range`() {
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
        val hideActionObject = ActionObject(
            "id_0",
            ActionType.HIDE_OVERLAY,
            5000L,
            parsedOverlayRelatedData,
            null,
            null
        )
        tvAnnotationFactory.setAnnotations(listOf(hideActionObject))

        tvAnnotationFactory.build(3500L, isPlaying = true, interrupted = false)

        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<HideOverlayActionEntity>())
    }

    @Test
    fun `setVariable test`() {
        val dataMap = buildMap<String, Any> {
            put("name", "\$awayScore")
            put("value", 0)
            put("type", "long")
            put("double_precision", 2)
        }
        val setVariableActionObject = ActionObject(
            "id_5",
            ActionType.SET_VARIABLE,
            5000L,
            null,
            null,
            dataMap
        )
        tvAnnotationFactory.setAnnotations(listOf(setVariableActionObject))

        tvAnnotationFactory.build(5000L, isPlaying = true, interrupted = false)

        Mockito.verify(tvAnnotationListener, never()).addOverlay(any())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<OverlayEntity>())
        Mockito.verify(tvAnnotationListener, never()).removeOverlay(any<HideOverlayActionEntity>())
        Mockito.verify(tvAnnotationListener).createVariable(any())


    }
}