package tv.mycujoo.mls.core

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.ActionSourceData
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.matcher.OverlayEntityMatcher
import java.util.concurrent.locks.ReentrantLock
import kotlin.test.assertTrue

@ExperimentalStdlibApi
class AnnotationFactoryTest {

    /**region subject under test*/
    private lateinit var annotationFactory: AnnotationFactory

    /**endregion */

    @Mock
    lateinit var annotationListener: IAnnotationListener

    @Mock
    lateinit var downloaderClient: IDownloaderClient

    @Mock
    lateinit var viewHandler: IViewHandler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val reentrantLock = ReentrantLock()
        annotationFactory = AnnotationFactory(annotationListener, downloaderClient, viewHandler, reentrantLock, reentrantLock.newCondition())
    }

    @Test
    fun `sort timer related actions based on priority`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceDataOfAdjustTimer = ActionSourceData("id_01", "adjust_timer", 5000L, dataMap)
        val actionSourceDataOfPauseTimer = ActionSourceData("id_01", "pause_timer", 5000L, dataMap)
        val actionSourceDataOfStartTimer = ActionSourceData("id_01", "start_timer", 5000L, dataMap)
        val actionSourceDataOfCreateTimer = ActionSourceData("id_01", "create_timer", 5000L, dataMap)
        val actionResponse = ActionResponse(
            listOf(
                actionSourceDataOfAdjustTimer,
                actionSourceDataOfPauseTimer,
                actionSourceDataOfStartTimer,
                actionSourceDataOfCreateTimer
            )
        )
        annotationFactory.setAnnotations(actionResponse)


        assertTrue { annotationFactory.actionList()[0].type == ActionType.CREATE_TIMER }
        assertTrue { annotationFactory.actionList()[1].type == ActionType.START_TIMER }
        assertTrue { annotationFactory.actionList()[2].type == ActionType.PAUSE_TIMER }
        assertTrue { annotationFactory.actionList()[3].type == ActionType.ADJUST_TIMER }
    }

    /**region Regular play mode*/
    @Test
    fun `given ShowOverlay action, should add overlay`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        annotationFactory.build(4001L, true, interrupted = false)


        verify(annotationListener).addOverlay(argThat(OverlayEntityMatcher("id_01")))
    }

    @Test
    fun `given HideOverlay action, should remove overlay`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
            put("duration", 10000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "hide_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)


        annotationFactory.build(15000L, true, interrupted = false)


        verify(annotationListener).removeOverlay(argThat(OverlayEntityMatcher("id_01")))
    }

    /**endregion */


    /**region Interrupted play mode*/
    @Test
    fun `given lingering intro overlay, should addOrUpdate overlay`() {
        val dataMap = buildMap<String, Any> {
            put("animatein_type", "fade_in")
            put("animatein_duration", 3000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        annotationFactory.build(5001L, true, interrupted = true)



        verify(annotationListener).addOrUpdateLingeringIntroOverlay(
            argThat(OverlayEntityMatcher("id_01")),
            any(),
            any()
        )

    }

    @Test
    fun `given lingering midway overlay, should addOrUpdate overlay`() {
        val dataMap = buildMap<String, Any> {}
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        annotationFactory.build(5001L, true, interrupted = true)


        verify(annotationListener).addOrUpdateLingeringMidwayOverlay(
            argThat(OverlayEntityMatcher("id_01"))
        )
    }

    @Test
    fun `given lingering outro overlay, should addOrUpdate overlay`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
            put("duration", 5000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        annotationFactory.build(11001L, true, interrupted = true)


        verify(annotationListener).addOrUpdateLingeringOutroOverlay(
            argThat(OverlayEntityMatcher("id_01")),
            any(),
            any()
        )
    }

    @Test
    fun `given overlay after current time in interrupted mode, should remove it`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)


        annotationFactory.build(0L, true, interrupted = true)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
    }

    @Test
    fun `given overlay before current time in interrupted mode, should remove it`() {
        val dataMap = buildMap<String, Any> {
            put("animateout_type", "fade_out")
            put("animateout_duration", 3000.toDouble())
        }
        val actionSourceData = ActionSourceData("id_01", "show_overlay", 5000L, dataMap)
        val actionResponse = ActionResponse(listOf(actionSourceData))
        annotationFactory.setAnnotations(actionResponse)


        annotationFactory.build(10000L, true, interrupted = true)


        verify(annotationListener).removeLingeringOverlay(argThat(OverlayEntityMatcher("id_01")))
    }

    /**endregion */

    companion object {
        private const val INVALID = -1L
        private const val ONE_SECONDS = 1000L
        private const val TWO_SECONDS = 2000L
        private const val FIVE_SECONDS = 5000L
        private const val FIFTEEN_SECONDS = 15000L
        private const val TWENTY_FIVE_SECONDS = 25000L
    }


}