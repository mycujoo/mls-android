package tv.mycujoo.mls.core

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.ActionSourceData
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.TestData.Companion.getSampleShowOverlayAction
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.matcher.ActionArgumentMatcher
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
    lateinit var variableKeeper: IVariableKeeper
    /**endregion */

    /**region Setup*/
    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(player.dvrWindowSize()).thenReturn(60000)

        annotationFactory = AnnotationFactory(
            annotationListener,
            variableKeeper
        )
    }
    /**endregion */

    /**region Sorting*/
    @Test
    fun `sort timer related actions based on priority`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceDataOfAdjustTimer =
            ActionSourceData("id_01", "adjust_timer", 5000L, -1L, dataMap)
        val actionSourceDataOfPauseTimer =
            ActionSourceData("id_01", "pause_timer", 5000L, -1L, dataMap)
        val actionSourceDataOfStartTimer =
            ActionSourceData("id_01", "start_timer", 5000L, -1L, dataMap)
        val actionSourceDataOfCreateTimer =
            ActionSourceData("id_01", "create_timer", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(
            listOf(
                actionSourceDataOfAdjustTimer,
                actionSourceDataOfPauseTimer,
                actionSourceDataOfStartTimer,
                actionSourceDataOfCreateTimer
            )
        )
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })


        assertTrue { annotationFactory.getCurrentActions()[0] is Action.CreateTimerAction }
        assertTrue { annotationFactory.getCurrentActions()[0] is Action.StartTimerAction }
        assertTrue { annotationFactory.getCurrentActions()[0] is Action.PauseTimerAction }
        assertTrue { annotationFactory.getCurrentActions()[0] is Action.AdjustTimerAction }
    }
    /**endregion */

    /**region Regular play mode, Relative time system*/
    @Test
    fun `given ShowOverlay action, should add overlay`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(4001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction)
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given HideOverlay action, should remove overlay`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "hide_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })


        val buildPoint = BuildPoint(4001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeOverlay("id_01", null)
        verify(annotationListener, never()).addOverlay(any())
    }
    /**endregion */

    /**region Regular play mode - Absolute time system*/
    @Test
    fun `given ShowOverlay action, should add overlay, absolute time system`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(4001L, 1605609886001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction)
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }


    @Test
    fun `given HideOverlay action, should remove overlay, absolute time system`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "hide_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609885000L)


        val buildPoint =
            BuildPoint(1001L, 1605609886001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeOverlay("id_01", null)
        verify(annotationListener, never()).removeOverlay(any(), null)
        verify(annotationListener, never()).addOverlay(any())
    }
    /**endregion */

    /**region Interrupted play mode, relative time system*/
    @Test
    fun `given lingering intro overlay, should addOrUpdate overlay, relative time system`() {
        val dataMap = buildMap<String, Any> {
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(5001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction,
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given lingering midway overlay, should addOrUpdate overlay, relative time system`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(5001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given lingering outro overlay, should addOrUpdate overlay, relative time system`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
            put("duration", 5000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(11001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction,
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given overlay after current time in interrupted mode, should remove it, relative time system`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(0L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay("id_01")
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    @Test
    fun `given overlay before current time in interrupted mode, should remove it, relative time system`() {
        val dataMap = buildMap<String, Any> {
            put("duration", 1000.toDouble())
            put("animateout_type", "fade_out")
            put("animateout_duration", 1000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
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
        val dataMap = buildMap<String, Any> {
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", -1, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(5001L, 1605609887001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction,
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringMidwayOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }


    @Test
    fun `given lingering midway overlay, should addOrUpdate overlay, absolute time system`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(5001L, 1605609887001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given lingering outro overlay, should addOrUpdate overlay, absolute time system`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
            put("duration", 5000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)
        whenever(player.dvrWindowStartTime()).thenReturn(1605609882000L)


        val buildPoint =
            BuildPoint(11001L, 1605609893001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(ActionArgumentMatcher("id_01")) as Action.ShowOverlayAction,
            any(),
            any()
        )
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given overlay after current time in interrupted mode, should remove it, absolute time system`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)

        getSampleShowOverlayAction(-1L, 1605609887000L)

        val actionResponse = ActionResponse(listOf())
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(0L, 1605609882000L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay("id_01")
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    @Test
    fun `given overlay before current time in interrupted mode, should remove it, absolute time system`() {
        val dataMap = buildMap<String, Any> {
            put("duration", 1000.toDouble())
            put("animateout_type", "fade_out")
            put("animateout_duration", 1000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
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