package tv.mycujoo.mls.core

import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.ActionSourceData
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.matcher.OverlayEntityMatcher
import tv.mycujoo.mls.player.IPlayer
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.assertTrue

@ExperimentalStdlibApi
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

    /**region setup*/
    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(player.dvrWindowSize()).thenReturn(60000)
        whenever(viewHandler.getVariableKeeper()).thenReturn(variableKeeper)

        val reentrantLock = ReentrantLock()
        annotationFactory = AnnotationFactory(
            annotationListener,
            viewHandler,
            reentrantLock,
            reentrantLock.newCondition()
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })


        assertTrue { annotationFactory.actionList()[0].type == ActionType.CREATE_TIMER }
        assertTrue { annotationFactory.actionList()[1].type == ActionType.START_TIMER }
        assertTrue { annotationFactory.actionList()[2].type == ActionType.PAUSE_TIMER }
        assertTrue { annotationFactory.actionList()[3].type == ActionType.ADJUST_TIMER }
    }
    /**endregion */

    /**region Regular play mode, Relative time system*/
    @Test
    fun `given ShowOverlay action, should add overlay`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(4001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(argThat(OverlayEntityMatcher("id_01")))
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }

    @Test
    fun `given HideOverlay action, should remove overlay`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
            put("duration", 10000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "hide_overlay", 5000L, -1L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })

        val buildPoint = BuildPoint(15001L, -1L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(4001L, 1605609886001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOverlay(argThat(OverlayEntityMatcher("id_01")))
        verify(annotationListener, never()).removeLingeringOverlay(any())
    }


    @Test
    fun `given HideOverlay action, should remove overlay, absolute time system`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
            put("duration", 10000.toDouble())
        }
        val actionSourceData =
            ActionSourceData("id_01", "hide_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(15001L, 1605609897001L, player, isPlaying = true, isInterrupted = false)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(5001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(OverlayEntityMatcher("id_01")),
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(5001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(OverlayEntityMatcher("id_01"))
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)


        val buildPoint = BuildPoint(11001L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(OverlayEntityMatcher("id_01")),
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(0L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)

        val buildPoint = BuildPoint(10000L, -1L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(5001L, 1605609887001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)



        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(OverlayEntityMatcher("id_01")),
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(5001L, 1605609887001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(OverlayEntityMatcher("id_01"))
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
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint =
            BuildPoint(11001L, 1605609893001L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(OverlayEntityMatcher("id_01")),
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
        val actionSourceData = ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint = BuildPoint(0L, 1605609882000L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
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
        val actionSourceData = ActionSourceData("id_01", "show_overlay", -1L, 1605609887000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
        whenever(player.isWithinValidSegment(any())).thenReturn(true)
        whenever(player.duration()).thenReturn(120000L)


        val buildPoint = BuildPoint(10000L, 1605609892000L, player, isPlaying = true, isInterrupted = true)
        annotationFactory.build(buildPoint)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
        verify(annotationListener, never()).addOverlay(any())
        verify(annotationListener, never()).addOrUpdateLingeringOutroOverlay(any(), any(), any())
    }

    /**endregion */

    companion object {
        private const val INVALID = -1L
        private const val ONE_SECONDS = ONE_SECOND_IN_MS
        private const val TWO_SECONDS = 2000L
        private const val FIVE_SECONDS = 5000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L
    }


}