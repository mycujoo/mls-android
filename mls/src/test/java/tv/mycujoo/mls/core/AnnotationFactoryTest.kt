package tv.mycujoo.mls.core

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.TestData.Companion.getSampleShowOverlayAction
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.matcher.ShowOverlayActionArgumentMatcher
import tv.mycujoo.mls.player.IPlayer
import kotlin.test.assertTrue

@OptIn(ExperimentalStdlibApi::class)
class AnnotationFactoryTest {

    /**region subject under test*/
    private lateinit var annotationFactory: AnnotationFactory

    /**endregion */

    /**region fields*/
    @Mock
    lateinit var annotationListener: IAnnotationListener

    @Mock
    lateinit var player: IPlayer

    @Mock
    lateinit var viewHandler: IViewHandler

    @Mock
    lateinit var variableKeeper: IVariableKeeper
    /**endregion */

    /**region Setup*/
    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(player.dvrWindowSize()).thenReturn(60000)

        annotationFactory = AnnotationFactory(
            annotationListener,
            viewHandler,
            variableKeeper
        )
    }
    /**endregion */

    /**region Sorting*/
    @Test
    fun `sort timer related actions based on priority`() {
        val adjustTimerAction = Action.AdjustTimerAction("id_01", 5000L, -1L, "name", 0L)
        val pauseTimerAction = Action.PauseTimerAction("id_02", 5000L, -1L, "name")
        val startTimerAction = Action.StartTimerAction("id_03", 5000L, -1L, "name")
        val createTimerAction =
            Action.CreateTimerAction("id_04", 5000L, -1L, "name", capValue = -1L)
        annotationFactory.setActions(
            listOf(
                adjustTimerAction,
                pauseTimerAction,
                startTimerAction,
                createTimerAction
            )
        )


        assertTrue { annotationFactory.getCurrentActions()[0] is Action.CreateTimerAction }
        assertTrue { annotationFactory.getCurrentActions()[1] is Action.StartTimerAction }
        assertTrue { annotationFactory.getCurrentActions()[2] is Action.PauseTimerAction }
        assertTrue { annotationFactory.getCurrentActions()[3] is Action.AdjustTimerAction }
    }
    /**endregion */

    /**region Regular play mode, Relative time system*/
    @Test
    fun `given ShowOverlayAction, should add overlay`() {
        val action = Action.ShowOverlayAction("id_01", 5000L, -1L, null)
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(4001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(argThat(ShowOverlayActionArgumentMatcher("id_01")))
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }

    /**region HideOverlayAction related*/
    @Test
    fun `given HideOverlayAction, should remove overlay`() {
        val action = Action.HideOverlayAction("id_01", 5000L, -1L, null, "cid_1001")
        annotationFactory.setActions(listOf(action))


        val buildPoint = BuildPoint(4001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeOverlay("cid_1001", null)
        verify(annotationListener, never()).addOverlay(any())
    }

    @Test
    fun `given HideOverlayAction with ReshowOverlayAction within range, should not remove overlay`() {
        val hideOverlayAction = Action.HideOverlayAction("id_01", 5000L, -1L, null, "cid_1001")
        val reshowOverlayAction = Action.ReshowOverlayAction("id_01", 5000L, -1L, "cid_1001")
        annotationFactory.setActions(listOf(hideOverlayAction, reshowOverlayAction))


        val buildPoint = BuildPoint(4001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener, never()).removeOverlay("cid_1001", null)
        verify(annotationListener, never()).addOverlay(any())
    }
    /**endregion */

    /**region ReshowOverlayAction related*/
    @Test
    fun `given ReshowOverlayAction, should show overlay`() {
        val showOverlayAction = getSampleShowOverlayAction(5000L, -1L) // id is cid_1001
        val reshowOverlayAction = Action.ReshowOverlayAction("id_01", 8000L, -1L, "cid_1001")
        annotationFactory.setActions(listOf(showOverlayAction, reshowOverlayAction))


        val buildPoint = BuildPoint(7001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringIntroOverlay(any(), any(), any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    @Test
    fun `given ReshowOverlayAction, without related ShowOverlay should not show overlay`() {
        val showOverlayAction = getSampleShowOverlayAction(5000L, -1L) // id is cid_1001
        val reshowOverlayAction = Action.ReshowOverlayAction("id_01", 8000L, -1L, "cid_1002")
        annotationFactory.setActions(listOf(showOverlayAction, reshowOverlayAction))


        val buildPoint = BuildPoint(7001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringIntroOverlay(any(), any(), any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }
    /**endregion */
    /**endregion */

    /**region Regular play mode - Absolute time system*/
    @Test
    fun `given ShowOverlay action, should add overlay, absolute time system`() {
        val action = Action.ShowOverlayAction("id_01", -1L, 1605609887000L)
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(4001L, 1605609886001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(argThat(ShowOverlayActionArgumentMatcher("id_01")))
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }


    @Test
    fun `given HideOverlay action, should remove overlay, absolute time system`() {
        val outroTransitionSpec = TransitionSpec(3000L, AnimationType.FADE_OUT, 3000L)
        val hideAction =
            Action.HideOverlayAction("id_01", -1L, 1605609887000L, outroTransitionSpec, "cid_01")
        annotationFactory.setActions(listOf(hideAction))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609885000L)


        val buildPoint =
            BuildPoint(1001L, 1605609886001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeOverlay("cid_01", outroTransitionSpec)
        verify(annotationListener, never()).addOverlay(any())
    }

    /**region ReshowOverlayAction related*/
    @Test
    fun `given ReshowOverlayAction, should show overlay, -absolute-time-system`() {
        val showOverlayAction = getSampleShowOverlayAction(0L, 1605609885000L) // id is cid_1001
        val reshowOverlayAction =
            Action.ReshowOverlayAction("id_01", 2000L, 1605609887000L, "cid_1001")
        annotationFactory.setActions(listOf(showOverlayAction, reshowOverlayAction))
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609885000L)


        val buildPoint =
            BuildPoint(1001L, 1605609886001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringIntroOverlay(any(), any(), any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    /**endregion */

    /**endregion */


    /**region Interrupted play mode, relative time system*/
    @Test
    fun `given lingering intro overlay, should addOrUpdate overlay, relative time system`() {
        val introTransitionSpec = TransitionSpec(5000L, AnimationType.FADE_IN, 3000L)
        val action =
            Action.ShowOverlayAction(
                id = "id_01",
                offset = 5000L,
                absoluteTime = -1L, introTransitionSpec = introTransitionSpec
            )
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(5001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(ShowOverlayActionArgumentMatcher("id_01")),
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }

    @Test
    fun `given lingering midway overlay, should addOrUpdate overlay, relative time system`() {
        val action = Action.ShowOverlayAction("id_01", 5000L, -1L)
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(5001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(ShowOverlayActionArgumentMatcher("id_01"))
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }

    @Test
    fun `given lingering outro overlay, should addOrUpdate overlay, relative time system`() {
        val outroTransitionSpec = TransitionSpec(10000L, AnimationType.FADE_OUT, 3000L)
        val action = Action.ShowOverlayAction(
            id = "id_01",
            offset = 5000L,
            absoluteTime = -1L,
            svgData = null,
            duration = 5000L, outroTransitionSpec = outroTransitionSpec
        )
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(11001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(ShowOverlayActionArgumentMatcher("id_01")),
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }

    @Test
    fun `given overlay after current time in interrupted mode, should remove it, relative time system`() {
        val action = Action.ShowOverlayAction("id_01", 5000L, -1L, null, 1000L)
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(0L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay("id_01")
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    @Test
    fun `given overlay before current time in interrupted mode, should remove it, relative time system`() {
        val action = Action.ShowOverlayAction("id_01", 5000L, -1L, null, 1000L)
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(10000L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay("id_01")
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    /**endregion */

    /**region Interrupted play mode, absolute time system*/
    @Test
    fun `given lingering intro overlay, should addOrUpdate overlay, absolute time system`() {
        val introTransitionSpec = TransitionSpec(0L, AnimationType.FADE_IN, 3000L)
        val action = Action.ShowOverlayAction(
            id = "id_01",
            offset = -1L,
            absoluteTime = 1605609887000L,
            introTransitionSpec = introTransitionSpec
        )
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(5001L, 1605609887001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(ShowOverlayActionArgumentMatcher("id_01")),
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }


    @Test
    fun `given lingering midway overlay, should addOrUpdate overlay, absolute time system`() {
        val action = Action.ShowOverlayAction(
            id = "id_01",
            offset = -1L,
            absoluteTime = 1605609887000L
        )
        annotationFactory.setActions(listOf(action))

        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(5001L, 1605609887001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(ShowOverlayActionArgumentMatcher("id_01"))
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }

    @Test
    fun `given lingering outro overlay, should addOrUpdate overlay, absolute time system`() {
        val outroTransitionSpec = TransitionSpec(0L, AnimationType.FADE_OUT, 5000L)
        val action = Action.ShowOverlayAction(
            id = "id_01",
            offset = -1L,
            absoluteTime = 1605609887000L,
            duration = 5000L,
            outroTransitionSpec = outroTransitionSpec
        )
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(11001L, 1605609893001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(ShowOverlayActionArgumentMatcher("id_01")),
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).removeLingeringOverlay(any(), any())
    }

    @Test
    fun `given overlay after current time in interrupted mode, should remove it, absolute time system`() {
        val outroTransitionSpec = TransitionSpec(3000L, AnimationType.FADE_OUT, 3000L)
        val action = Action.ShowOverlayAction(
            id = "id_01",
            offset = -1L,
            absoluteTime = 1605609887000L,
            svgData = null,
            outroTransitionSpec = outroTransitionSpec
        )
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(0L, 1605609882000L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay("id_01", outroTransitionSpec)
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    @Test
    fun `given overlay before current time in interrupted mode, should remove it, absolute time system`() {
        val action = Action.ShowOverlayAction("id_01", -1L, 1605609887000L, null, 1000L)
        annotationFactory.setActions(listOf(action))
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(10000L, 1605609892000L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay("id_01")
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    /**endregion */
}