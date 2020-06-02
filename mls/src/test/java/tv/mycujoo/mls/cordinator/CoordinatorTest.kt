package tv.mycujoo.mls.cordinator

import android.os.Handler
import com.google.android.exoplayer2.SimpleExoPlayer
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.TestSampleData
import tv.mycujoo.mls.TestSampleData.Companion.getSampleCommandAction
import tv.mycujoo.mls.TestSampleData.Companion.getSampleShowAnnouncementOverlayAction
import tv.mycujoo.mls.core.AnnotationPublisher
import tv.mycujoo.mls.entity.actions.AbstractAction
import tv.mycujoo.mls.entity.actions.ActionRootSourceData
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.HighlightAdapter
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import kotlin.test.assertEquals


class CoordinatorTest {

    lateinit var coordinator: Coordinator

    @Mock
    lateinit var api: Api

    @Mock
    lateinit var publisher: AnnotationPublisher

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var handler: Handler

    @Mock
    lateinit var highlightAdapter: HighlightAdapter

    @Mock
    lateinit var playerViewWrapper: PlayerViewWrapper

    lateinit var coordinator: Coordinator

    @Mock
    lateinit var api: Api

    @Mock
    lateinit var widget: PlayerViewWrapper
    private var publisher = AnnotationPublisherImpl()

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var handler: Handler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(api.getAnnotations()).thenReturn(emptyList())
        whenever(api.getActions()).thenReturn(getListOfActionWrapper())

        coordinator = Coordinator(api, publisher)
        coordinator.initialize(exoPlayer, handler, highlightAdapter)
        coordinator.playerViewWrapper = playerViewWrapper

    }

    @Test
    fun syncingOverlaysWithTime() {


        coordinator.annotationBuilder.setCurrentTime(0L, true)
        coordinator.annotationBuilder.buildPendingAnnotationsForCurrentTime()

        coordinator.annotationBuilder.setCurrentTime(1000L, true)
        coordinator.annotationBuilder.buildPendingAnnotationsForCurrentTime()


        coordinator.playerViewWrapper.showAnnouncementOverLay(getSampleShowAnnouncementOverlayAction())
    }

    @Test
    fun `should AddOverlay & NOT remove or hide OnSeeking to 10th second`() {
        seekTo(10000L)

        coordinator.seekInterruptionEventListener.onPositionDiscontinuity(1)


        val captor = argumentCaptor<ActionWrapper>()
        verify(publisher, times(1)).onNewActionWrapperAvailable(captor.capture())
        assertEquals(10000L, captor.firstValue.offset)

        verify(publisher, never()).onNewRemovalOrHidingActionAvailable(any())
    }

    @Test
    fun `should remove or hide all applicable actions OnSeeking to 20th second`() {
        seekTo(20000L)

        coordinator.seekInterruptionEventListener.onPositionDiscontinuity(1)


        val captor = argumentCaptor<ActionWrapper>()
        verify(publisher, never()).onNewActionWrapperAvailable(captor.capture())

        val captorForRemoveCommand = argumentCaptor<ActionWrapper>()
        verify(publisher, times(2)).onNewRemovalOrHidingActionAvailable(captorForRemoveCommand.capture())
        assertEquals(10000L, captorForRemoveCommand.firstValue.offset)
        assertEquals(20000L, captorForRemoveCommand.secondValue.offset)
    }

    @Test
    fun `should remove or hide all applicable actions OnSeeking to 30th second`() {
        seekTo(30000L)

        coordinator.seekInterruptionEventListener.onPositionDiscontinuity(1)


        val captor = argumentCaptor<ActionWrapper>()
        verify(publisher, never()).onNewActionWrapperAvailable(captor.capture())

        val captorForRemoveCommand = argumentCaptor<ActionWrapper>()
        verify(publisher, times(3)).onNewRemovalOrHidingActionAvailable(captorForRemoveCommand.capture())
        assertEquals(10000L, captorForRemoveCommand.firstValue.offset)
        assertEquals(20000L, captorForRemoveCommand.secondValue.offset)
        assertEquals(30000L, captorForRemoveCommand.thirdValue.offset)
    }


    private fun seekTo(time: Long) {
        whenever(exoPlayer.currentPosition).thenReturn(time)
    }

    fun getListOfActionWrapper(): List<ActionWrapper> {
        val listOfActionWrapper = ArrayList<ActionWrapper>()

        val firstActionRootSourceData = ActionRootSourceData().apply {
            id = "id_1000"
            time = 10000L
            actionsList.add(getSampleShowAnnouncementOverlayAction())
            actionsList.add(TestSampleData.getSampleShowScoreboardAction())
            actionsList.add(TestSampleData.getSampleShowScoreboardAction_WithDismissingParams())
        }

        firstActionRootSourceData.build()

        firstActionRootSourceData.actionsList.forEach { abstractAction: AbstractAction ->
            val actionWrapper = ActionWrapper()
            actionWrapper.action = abstractAction
            actionWrapper.offset = firstActionRootSourceData.time!!

            listOfActionWrapper.add(actionWrapper)
        }

        val secondActionRootSourceData = ActionRootSourceData().apply {
            id = "id_1001"
            time = 20000L
            actionsList.add(getSampleCommandAction("remove").apply {
                targetViewId = "action_view_id_10000"
            })
        }

        secondActionRootSourceData.actionsList.forEach { abstractAction: AbstractAction ->
            val actionWrapper = ActionWrapper()
            actionWrapper.action = abstractAction
            actionWrapper.offset = secondActionRootSourceData.time!!

            listOfActionWrapper.add(actionWrapper)
        }

        val thirdActionRootSourceData = ActionRootSourceData().apply {
            id = "id_1002"
            time = 30000L
            actionsList.add(getSampleCommandAction("hide").apply {
                targetViewId = "action_view_id_10001"
            })
        }

        thirdActionRootSourceData.build()

        thirdActionRootSourceData.actionsList.forEach { abstractAction: AbstractAction ->
            val actionWrapper = ActionWrapper()
            actionWrapper.action = abstractAction
            actionWrapper.offset = thirdActionRootSourceData.time!!

            listOfActionWrapper.add(actionWrapper)
        }

        return listOfActionWrapper
    }
}