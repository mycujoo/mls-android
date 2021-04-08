package tv.mycujoo.mcls.api

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.SimpleExoPlayer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.core.VideoPlayerMediator
import tv.mycujoo.mcls.widgets.MLSPlayerView
import kotlin.test.assertEquals

class VideoPlayerTest {

    private lateinit var videoPlayer: VideoPlayer

    private lateinit var eventListener: Player.EventListener

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var audioComponent: Player.AudioComponent

    @Mock
    lateinit var videoPlayerMediator: VideoPlayerMediator

    @Mock
    lateinit var MLSPlayerView: MLSPlayerView

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(exoPlayer.addListener(any<Player.EventListener>())).then { i ->
            eventListener = i.getArgument<Player.EventListener>(0)
            eventListener
        }
        videoPlayer = VideoPlayer(exoPlayer, videoPlayerMediator, MLSPlayerView)
        whenever(exoPlayer.audioComponent).thenReturn(audioComponent)
    }

    @Test
    fun `test play`() {
        videoPlayer.play()

        verify(exoPlayer).playWhenReady = true
    }

    @Test
    fun `test pause`() {
        videoPlayer.pause()

        verify(exoPlayer).playWhenReady = false
    }

    @Test
    fun `test seekTo`() {
        videoPlayer.seekTo(0)
        videoPlayer.seekTo(42)

        verify(exoPlayer).seekTo(0L)
        verify(exoPlayer).seekTo(42000L)
    }

    @Test
    fun `test currentTime for under 1 second`() {
        whenever(exoPlayer.currentPosition).thenReturn(42L)

        assertEquals(0, videoPlayer.currentTime())
    }

    @Test
    fun `test currentTime for above 1 second`() {
        whenever(exoPlayer.currentPosition).thenReturn(42000L)

        assertEquals(42, videoPlayer.currentTime())
    }

    @Test
    fun `test currentTime for 0 second`() {
        whenever(exoPlayer.currentPosition).thenReturn(0L)

        assertEquals(0, videoPlayer.currentTime())
    }

    @Test
    fun `test optimisticCurrentTime without prior seekTo`() {
        whenever(exoPlayer.currentPosition).thenReturn(42L)

        assertEquals(0, videoPlayer.optimisticCurrentTime())
    }

    @Test
    fun `test optimisticCurrentTime with prior seekTo, before loading content`() {
        whenever(exoPlayer.currentPosition).thenReturn(42L)
        videoPlayer.seekTo(15)
        eventListener.onPlayerStateChanged(true, STATE_BUFFERING)

        assertEquals(15, videoPlayer.optimisticCurrentTime())
    }

    @Test
    fun `test optimisticCurrentTime with prior seekTo, after loading content`() {
        whenever(exoPlayer.currentPosition).thenReturn(42L)
        videoPlayer.seekTo(15)
        eventListener.onPlayerStateChanged(true, STATE_READY)


        assertEquals(0, videoPlayer.optimisticCurrentTime())
    }

    @Test
    fun `test invalid currentTime`() {
        whenever(exoPlayer.currentPosition).thenReturn(-1L)
        assertEquals(-1, videoPlayer.currentTime())
    }

    @Test
    fun `test currentDuration for under 1 second`() {
        whenever(exoPlayer.duration).thenReturn(42L)

        assertEquals(0, videoPlayer.currentDuration())
    }

    @Test
    fun `test currentDuration for above 1 second`() {
        whenever(exoPlayer.duration).thenReturn(42000L)

        assertEquals(42, videoPlayer.currentDuration())
    }

    @Test
    fun `test currentDuration for 0 second`() {
        whenever(exoPlayer.duration).thenReturn(0L)

        assertEquals(0, videoPlayer.currentDuration())
    }

    @Test
    fun `test invalid currentDuration`() {
        whenever(exoPlayer.duration).thenReturn(-1L)
        assertEquals(-1, videoPlayer.currentDuration())
    }

    @Test
    fun `test isMuted true`() {
        whenever(audioComponent.volume).thenReturn(0F)

        assertEquals(true, videoPlayer.isMuted())
    }

    @Test
    fun `test isMuted false`() {
        whenever(audioComponent.volume).thenReturn(1F)

        assertEquals(false, videoPlayer.isMuted())
    }

    @Test
    fun `test muting audio`() {
        videoPlayer.mute()

        verify(audioComponent).volume = 0F
    }

    @Test
    fun `test showEventInfoOverlay`() {
        videoPlayer.showEventInfoOverlay()

        verify(MLSPlayerView).showStartedEventInformationDialog()
    }

    @Test
    fun `test hideEventInfoOverlay`() {
        videoPlayer.hideEventInfoOverlay()


        verify(MLSPlayerView).hideInfoDialogs()
    }

    @Test
    fun `given request to play event with id, should call videoPlayerMediator`() {
        videoPlayer.playVideo("42")

        verify(videoPlayerMediator).playVideo("42")
    }

    @Test
    fun `given request to play event with eventEntity, should call videoPlayerMediator`() {
        val event = getSampleEventEntity()
        videoPlayer.playVideo(event)

        verify(videoPlayerMediator).playVideo(event)
    }

    private fun getSampleEventEntity(): EventEntity {
        val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
        return EventEntity(
            "42",
            "",
            "",
            "",
            null,
            location,
            "",
            null,
            EventStatus.EVENT_STATUS_UNSPECIFIED,
            emptyList(),
            "",
            emptyList(),
            Metadata(),
            false
        )
    }


}