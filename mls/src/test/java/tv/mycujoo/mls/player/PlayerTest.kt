package tv.mycujoo.mls.player

import android.os.Handler
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerTest {

    private lateinit var player: Player

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var mediaFactory: MediaFactory

    @Mock
    lateinit var handler: Handler

    @Mock
    lateinit var mediaOnLoadCompletedListener: MediaOnLoadCompletedListener

    @Mock
    lateinit var mediaItem: MediaItem

    @Mock
    lateinit var hlsMediaSource: HlsMediaSource


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        player = Player()
    }

    private fun initPlayer() {
        player.create(null, mediaFactory, exoPlayer, handler, mediaOnLoadCompletedListener)
    }

    @Test
    fun `when player is not created, should return not ready`() {
        // not initialized


        assertFalse(player.isReady())
    }

    @Test
    fun `when player is destroyed, should return not ready`() {
        initPlayer()


        player.release()


        assertFalse(player.isReady())
    }


    @Test
    fun `given listener, should attach it to exo player`() {
        initPlayer()
        val listener = object : com.google.android.exoplayer2.Player.EventListener {}


        player.addListener(listener)


        verify(exoPlayer).addListener(listener)
    }

    @Test
    fun `given seek offset, should call seek on exo player`() {
        initPlayer()


        player.seekTo(42L)


        verify(exoPlayer).seekTo(42L)
    }

    @Test
    fun `given uninitialized state, should return -1 as current position`() {
        //not initialized

        assertEquals(-1L, player.currentPosition())
    }

    @Test
    fun `given destroyed state, should return -1 as current position`() {
        initPlayer()


        player.release()


        assertEquals(-1L, player.currentPosition())
    }


    @Test
    fun `given valid state, should return exo player position as current position`() {
        initPlayer()
        whenever(exoPlayer.currentPosition).thenReturn(42L)


        assertEquals(42L, player.currentPosition())
    }

    @Test
    fun `absolute time test at the beginning`() {
        initPlayer()
        whenever(exoPlayer.currentPosition).thenReturn(0L)
        whenever(mediaOnLoadCompletedListener.getWindowStartTime()).thenReturn(1605609882000L)

        assertEquals(1605609882000L, player.currentAbsoluteTime())
    }

    @Test
    fun `absolute time test after beginning`() {
        initPlayer()
        whenever(exoPlayer.currentPosition).thenReturn(4000L)
        whenever(mediaOnLoadCompletedListener.getWindowStartTime()).thenReturn(1605609882000L)

        assertEquals(1605609886000L, player.currentAbsoluteTime())
    }

    @Test
    fun `absolute time test without window-start-time`() {
        initPlayer()
        whenever(exoPlayer.currentPosition).thenReturn(4000L)
        whenever(mediaOnLoadCompletedListener.getWindowStartTime()).thenReturn(-1L)

        assertEquals(-1L, player.currentAbsoluteTime())
    }

    @Test
    fun `dvrWindow-start-time valid`() {
        initPlayer()
        whenever(mediaOnLoadCompletedListener.getWindowStartTime()).thenReturn(1605609882000L)

        assertEquals(1605609882000L, player.dvrWindowStartTime())
    }

    @Test
    fun `dvrWindow-start-time invalid`() {
        initPlayer()
        whenever(mediaOnLoadCompletedListener.getWindowStartTime()).thenReturn(-1L)

        assertEquals(-1L, player.dvrWindowStartTime())
    }

    @Test
    fun `given uninitialized state, should return -1 as duration`() {
        //not initialized

        assertEquals(-1L, player.duration())
    }

    @Test
    fun `given destroyed state, should return -1 as duration`() {
        initPlayer()


        player.release()


        assertEquals(-1L, player.duration())
    }


    @Test
    fun `given valid state, should return exo player duration as duration`() {
        initPlayer()
        whenever(exoPlayer.duration).thenReturn(42L)


        assertEquals(42L, player.duration())
    }

    @Test
    fun `given valid state, should return exo player live state as isLive`() {
        initPlayer()
        whenever(exoPlayer.isCurrentWindowDynamic).thenReturn(true)
        whenever(exoPlayer.contentPosition).thenReturn(1000L)


        assertEquals(true, player.isLive())
    }

    @Test
    fun `given invalid state, should return exo player live state as isLive`() {
        // not initialized

        assertEquals(false, player.isLive())
    }

    @Test
    fun `while exo player is playing, should return is playing true`() {
        initPlayer()
        whenever(exoPlayer.isPlaying).thenReturn(true)


        assert(player.isPlaying())
    }

    @Test
    fun `while exo player is not playing, should return is playing false`() {
        initPlayer()
        whenever(exoPlayer.isPlaying).thenReturn(false)


        assert(player.isPlaying().not())
    }

    @Test
    fun `given uri to play when no resume data is available, should start from the beginning`() {
        initPlayer()
        whenever(mediaFactory.createMediaItem(any())).thenReturn(mediaItem)
        whenever(mediaFactory.createHlsMediaSource(any())).thenReturn(hlsMediaSource)

        player.play(MediaData(SAMPLE_URI, Long.MAX_VALUE, true))


        val mediaSourceCaptor = argumentCaptor<MediaSource>()
        val resetPositionCaptor = argumentCaptor<Boolean>()
        verify(exoPlayer).setMediaSource(mediaSourceCaptor.capture(), resetPositionCaptor.capture())
        assertTrue { resetPositionCaptor.firstValue }

    }

    @Test
    fun `given uri to play when resume data is available, should start from resume position`() {
        initPlayer()
        whenever(mediaFactory.createMediaItem(any())).thenReturn(mediaItem)
        whenever(mediaFactory.createHlsMediaSource(any())).thenReturn(hlsMediaSource)
        whenever(exoPlayer.currentWindowIndex).thenReturn(0)
        whenever(exoPlayer.isCurrentWindowSeekable).thenReturn(true)
        whenever(exoPlayer.currentPosition).thenReturn(42L)
        player.release()
        initPlayer()


        player.play(MediaData(SAMPLE_URI, Long.MAX_VALUE, true))


        val mediaItemCaptor = argumentCaptor<MediaItem>()
        val resetPositionCaptor = argumentCaptor<Boolean>()
        verify(exoPlayer).setMediaItem(mediaItemCaptor.capture(), resetPositionCaptor.capture())
        assertFalse { resetPositionCaptor.firstValue }
    }

    @Test
    fun `given discontinuity segment, should add it to discontinuity array`() {
        initPlayer()
        whenever(mediaFactory.createHlsMediaSource(any())).thenReturn(hlsMediaSource)
        whenever(exoPlayer.setMediaSource(any(), any<Boolean>())).then {
            val mediaSource = it.arguments[0] as MediaSource
            mediaSource
        }


    }

    @Test
    fun `isWithinValidSegment() empty list`() {
        initPlayer()
        val copyOnWriteArrayList = CopyOnWriteArrayList<Pair<Long, Long>>()
        whenever(mediaOnLoadCompletedListener.getDiscontinuityBoundaries()).thenReturn(
            copyOnWriteArrayList
        )


        assertTrue { player.isWithinValidSegment(5000000L)!! }
    }

    @Test
    fun `isWithinValidSegment() with discontinuity segment`() {
        initPlayer()
        val copyOnWriteArrayList = CopyOnWriteArrayList<Pair<Long, Long>>()
        // 1605693500 is Wednesday, 18 November 2020 09:58:20
        copyOnWriteArrayList.add(Pair(1605693500, 10))
        whenever(mediaOnLoadCompletedListener.getDiscontinuityBoundaries()).thenReturn(
            copyOnWriteArrayList
        )


        assertTrue { player.isWithinValidSegment(1605693498)!! }
        assertTrue { player.isWithinValidSegment(1605693499)!! }
        assertFalse { player.isWithinValidSegment(1605693500)!! }
        assertFalse { player.isWithinValidSegment(1605693505)!! }
        assertFalse { player.isWithinValidSegment(1605693510)!! }
        assertTrue { player.isWithinValidSegment(1605693511)!! }
    }

    @Test
    fun `isWithinValidSegment() with multiple discontinuity segments`() {
        initPlayer()
        val copyOnWriteArrayList = CopyOnWriteArrayList<Pair<Long, Long>>()
        // 1605693500 is Wednesday, 18 November 2020 09:58:20
        copyOnWriteArrayList.add(Pair(1605693500, 10))
        // 1605694500 is Wednesday, 18 November 2020 10:15:00
        copyOnWriteArrayList.add(Pair(1605694500, 5))

        whenever(mediaOnLoadCompletedListener.getDiscontinuityBoundaries()).thenReturn(
            copyOnWriteArrayList
        )


        assertTrue { player.isWithinValidSegment(1605693498)!! }
        assertTrue { player.isWithinValidSegment(1605693499)!! }
        assertFalse { player.isWithinValidSegment(1605693500)!! }
        assertFalse { player.isWithinValidSegment(1605693505)!! }
        assertFalse { player.isWithinValidSegment(1605693510)!! }
        assertTrue { player.isWithinValidSegment(1605693511)!! }

        assertTrue { player.isWithinValidSegment(1605694498)!! }
        assertTrue { player.isWithinValidSegment(1605694499)!! }
        assertFalse { player.isWithinValidSegment(1605694500)!! }
        assertFalse { player.isWithinValidSegment(1605694505)!! }
        assertTrue { player.isWithinValidSegment(1605694506)!! }
        assertTrue { player.isWithinValidSegment(1605694507)!! }
    }

    @Test
    fun pause() {
        initPlayer()
        player.pause()

        verify(exoPlayer).playWhenReady = false
    }

    @Test
    fun play() {
        initPlayer()
        player.play()

        verify(exoPlayer).playWhenReady = true
    }

    companion object {
        const val SAMPLE_URI = "https://mycujoo.tv"
    }
}