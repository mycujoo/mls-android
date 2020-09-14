package tv.mycujoo.mls.mediator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.core.IActionBuilder
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.manager.TimerKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.widgets.MLSPlayerView
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
    private lateinit var eventListener: Player.EventListener

    @Mock
    lateinit var playerView: MLSPlayerView

    @Mock
    lateinit var viewHandler: IViewHandler

    @Mock
    lateinit var timerKeeper: TimerKeeper

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
    lateinit var actionBuilder: IActionBuilder

    /**endregion */

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testCoroutineScope = TestCoroutineScope()

        whenever(viewHandler.getTimerKeeper()).thenReturn(timerKeeper)
        whenever(viewHandler.getVariableTranslator()).thenReturn(variableTranslator)

        whenever(
            scheduledExecutorService.scheduleAtFixedRate(
                any<Runnable>(),
                eq(1000L),
                eq(1000L),
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
            eventListener = it.getArgument(0) as Player.EventListener
            true
        }

        annotationMediator =
            AnnotationMediator(
                playerView,
                actionBuilder,
                player,
                scheduledExecutorService,
                handler
            )

        heartBeatOuterRunnable.run()
    }

    /**region Heart-beat tests*/
    @Test
    fun `heart beat initialization`() {
        assert(this::heartBeatOuterRunnable.isInitialized)
        assert(this::heartBeatInnerRunnable.isInitialized)
        assert(this::eventListener.isInitialized)
        verify(scheduledExecutorService).scheduleAtFixedRate(any(), eq(1000L), eq(1000L), eq(TimeUnit.MILLISECONDS))
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


        verify(actionBuilder, never()).setCurrentTime(any(), any())
        verify(actionBuilder, never()).buildCurrentTimeRange()
        verify(actionBuilder, never()).processTimers()
        verify(actionBuilder, never()).computeVariableNameValueTillNow()
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


        verify(actionBuilder).setCurrentTime(123L, true)
        verify(actionBuilder).buildCurrentTimeRange()
        verify(actionBuilder).processTimers()
        verify(actionBuilder).computeVariableNameValueTillNow()
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


        verify(actionBuilder).setCurrentTime(123L, true)
        verify(playerView).updateTime(123L, 400L)
    }

    @Test
    fun `given ready in onPlayerStateChanged of player, should update timers`() {
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)


        eventListener.onPlayerStateChanged(true, Player.STATE_READY)


        verify(playerView).updateTime(123L, 400L)
    }

    @Test
    fun `given ready in onPlayerStateChanged of player, with pending seek, should build lingering overlays`() {
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)
        eventListener.onPositionDiscontinuity(Player.DISCONTINUITY_REASON_SEEK) // make seek = true


        eventListener.onPlayerStateChanged(true, Player.STATE_READY)


        actionBuilder.processTimers()
        verify(actionBuilder).buildLingerings()
        verify(actionBuilder).buildCurrentTimeRange()
        verify(actionBuilder).computeVariableNameValueTillNow()
        verify(actionBuilder).computeVariableNameValueTillNow()
    }

    @Test
    fun `given ready in onPlayerStateChanged of player, without pending seek, should build lingering overlays`() {
        whenever(player.currentPosition()).thenReturn(123L)
        whenever(player.duration()).thenReturn(400L)
        // seek = false // default


        eventListener.onPlayerStateChanged(true, Player.STATE_READY)


        actionBuilder.processTimers()
        verify(actionBuilder, never()).buildLingerings()
        verify(actionBuilder, never()).buildCurrentTimeRange()
        verify(actionBuilder, never()).computeVariableNameValueTillNow()
        verify(actionBuilder, never()).computeVariableNameValueTillNow()
    }

    @Test
    fun `given isPlaying change in player, should process timers`() {
        eventListener.onIsPlayingChanged(true)
        eventListener.onIsPlayingChanged(false)


        verify(actionBuilder, times(2)).processTimers()
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


        verify(actionBuilder).setCurrentTime(123L, true)
        verify(actionBuilder).removeAll()
        verify(actionBuilder).buildCurrentTimeRange()
        verify(actionBuilder).buildLingerings()
    }

    @Test
    fun `should shutdown scheduler on relase`() {
        annotationMediator.release()

        verify(scheduledExecutorService).shutdown()
    }

    /**endregion */
}