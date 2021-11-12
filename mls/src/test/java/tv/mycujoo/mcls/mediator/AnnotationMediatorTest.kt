package tv.mycujoo.mcls.mediator

import android.os.Handler
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.helper.IDownloaderClient
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.widgets.MLSPlayerView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class AnnotationMediatorTest {


    /**region Subject under test*/
    private lateinit var annotationMediator: AnnotationMediator

    /**endregion */

    /**region Fields*/
    private lateinit var testCoroutineScope: TestCoroutineScope
    private lateinit var heartBeatOuterRunnable: Runnable
    private lateinit var heartBeatInnerRunnable: Runnable
    private lateinit var eventListener: Player.Listener

    @Mock
    lateinit var playerView: MLSPlayerView

    @Mock
    lateinit var viewHandler: IViewHandler

    @Mock
    lateinit var variableKeeper: VariableKeeper

    @Mock
    lateinit var variableTranslator: VariableTranslator

    @Mock
    lateinit var dataManager: IDataManager

    @Mock
    lateinit var player: IPlayer

    @Mock
    lateinit var scheduledExecutorService: ScheduledExecutorService

    @Mock
    lateinit var scheduledFuture: ScheduledFuture<Boolean>

    @Mock
    lateinit var handler: Handler

    @Mock
    lateinit var downloaderClient: IDownloaderClient

    @Mock
    lateinit var annotationFactory: IAnnotationFactory

    /**endregion */

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testCoroutineScope = TestCoroutineScope()

        whenever(
            scheduledExecutorService.scheduleAtFixedRate(
                any<Runnable>(),
                eq(ONE_SECOND_IN_MS),
                eq(ONE_SECOND_IN_MS),
                eq(TimeUnit.MILLISECONDS)
            )
        ).then {
            heartBeatOuterRunnable = it.getArgument(0) as Runnable
            scheduledFuture
        }

        whenever(handler.post(any())).then {
            heartBeatInnerRunnable = it.getArgument(0) as Runnable
            true
        }

        whenever(player.addListener(any())).then {
            eventListener = it.getArgument(0) as Player.Listener
            true
        }

        annotationMediator = AnnotationMediator(
            annotationFactory,
            dataManager,
            testCoroutineScope,
            scheduledExecutorService,
            Logger(LogLevel.MINIMAL)
        )
        annotationMediator.initialize(player, handler)
        annotationMediator.initPlayerView(playerView)

        heartBeatOuterRunnable.run()
    }

    /**region Heart-beat tests*/
    @Test
    fun `heart beat initialization`() {
        assert(this::heartBeatOuterRunnable.isInitialized)
        assert(this::heartBeatInnerRunnable.isInitialized)
        assert(this::eventListener.isInitialized)
        verify(scheduledExecutorService).scheduleAtFixedRate(
            any(),
            eq(ONE_SECOND_IN_MS),
            eq(ONE_SECOND_IN_MS),
            eq(TimeUnit.MILLISECONDS)
        )
    }

    @Test
    fun `heart beat should check player playing status`() {
        heartBeatInnerRunnable.run()

        verify(player).isPlaying()
    }

    @Test
    fun `given stopped player, should not call action builder`() {
        whenever(player.isPlaying()).thenReturn(false)
        heartBeatInnerRunnable.run()


//        verify(annotationFactory, never()).setCurrentTime(any(), any())
//        verify(annotationFactory, never()).buildCurrentTimeRange()
//        verify(annotationFactory, never()).processTimers()
//        verify(annotationFactory, never()).computeVariableNameValueTillNow()
    }

    @Test
    fun `given stopped player, should not update time`() {
        whenever(player.isPlaying()).thenReturn(false)
        heartBeatInnerRunnable.run()


        verify(playerView, never()).updateTime(any(), any())
    }

    @Test
    fun `given playing player, should call action builder`() {
        whenever(player.isPlaying()).thenReturn(true)
        whenever(player.currentPosition()).thenReturn(123L)
        heartBeatInnerRunnable.run()


//        verify(annotationFactory).setCurrentTime(123L, true)
//        verify(annotationFactory).buildCurrentTimeRange()
//        verify(annotationFactory).processTimers()
//        verify(annotationFactory).computeVariableNameValueTillNow()
    }

    @Test
    fun `given playing player, should update time`() {
        whenever(player.isPlaying()).thenReturn(false)
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)
        heartBeatInnerRunnable.run()


        verify(playerView, never()).updateTime(123L, 400L)
    }
    /**endregion */

    /**region Event listener tests*/
    @Test
    fun `given onPositionDiscontinuity, should update time`() {
        whenever(player.isPlaying()).thenReturn(true)
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)


        eventListener.onPositionDiscontinuity(Player.DISCONTINUITY_REASON_SEEK)


//        verify(annotationFactory).setCurrentTime(123L, true)
        verify(playerView).updateTime(123L, 400L)
    }

    @Test
    fun `given ready in onPlayerStateChanged of player, should update timers`() {
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)


        eventListener.onPlayWhenReadyChanged(true, Player.STATE_READY)


        verify(playerView).updateTime(123L, 400L)
    }

    @Test
    fun `given ready in onPlayerStateChanged of player, with pending seek, should build lingering overlays`() {
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)
        eventListener.onPositionDiscontinuity(Player.DISCONTINUITY_REASON_SEEK) // make seek = true


        eventListener.onPlayWhenReadyChanged(true, Player.STATE_READY)


//        annotationFactory.processTimers()
//        verify(annotationFactory).buildLingerings()
//        verify(annotationFactory).buildCurrentTimeRange()
//        verify(annotationFactory).computeVariableNameValueTillNow()
//        verify(annotationFactory).computeVariableNameValueTillNow()
    }

    @Test
    fun `given ready in onPlayerStateChanged of player, without pending seek, should build lingering overlays`() {
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)
        // seek = false // default


        eventListener.onPlayWhenReadyChanged(true, Player.STATE_READY)


//        annotationFactory.processTimers()
//        verify(annotationFactory, never()).buildLingerings()
//        verify(annotationFactory, never()).buildCurrentTimeRange()
//        verify(annotationFactory, never()).computeVariableNameValueTillNow()
//        verify(annotationFactory, never()).computeVariableNameValueTillNow()
    }

    @Test
    fun `given isPlaying change in player, should process timers`() {
        eventListener.onIsPlayingChanged(true)
        eventListener.onIsPlayingChanged(false)


//        verify(annotationFactory, times(2)).processTimers()
    }
    /**endregion */

    /**region Overridden functions tests*/
    @Test
    fun `given playerView to attach, should initialize onSizeChangeCallback`() {
        whenever(player.isPlaying()).thenReturn(true)
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)

        var onSizeChangedCallback = {}
        whenever(playerView.setOnSizeChangedCallback(any())).then {
            onSizeChangedCallback = it.getArgument(0) as () -> Unit
            true
        }
        annotationMediator.initPlayerView(playerView)


        onSizeChangedCallback.invoke()


//        verify(annotationFactory).setCurrentTime(123L, true)
//        verify(annotationFactory).removeAll()
//        verify(annotationFactory).buildCurrentTimeRange()
//        verify(annotationFactory).buildLingerings()
    }

    @Test
    fun `should shutdown scheduler on relase`() {
        annotationMediator.release()

        verify(scheduledExecutorService).shutdown()
    }

    /**endregion */
}