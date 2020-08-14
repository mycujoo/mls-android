package tv.mycujoo.mls.player

import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PlayerTest {

    private lateinit var player: Player

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var mediaFactory: HlsMediaSource.Factory

    @Mock
    lateinit var hlsMediaSource: HlsMediaSource


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        player = Player()
    }

    private fun initPlayer() {
        player.create(mediaFactory, exoPlayer)
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


        assertEquals(true, player.isLive())
    }

    @Test
    fun `given invalid state, should return exo player live state as isLive`() {
        // not initialized

        assertEquals(false, player.isLive())
    }


    @Test
    fun `given uri to play when no resume data is available, should start from the beginning`() {
        initPlayer()
        whenever(mediaFactory.createMediaSource(any())).thenReturn(hlsMediaSource)

        player.play(SAMPLE_URI)


        verify(exoPlayer).prepare(hlsMediaSource, true, false)
    }

    @Test
    fun `given uri to play when resume data is available, should start from resume position`() {
        initPlayer()
        whenever(mediaFactory.createMediaSource(any())).thenReturn(hlsMediaSource)
        whenever(exoPlayer.currentWindowIndex).thenReturn(0)
        whenever(exoPlayer.isCurrentWindowSeekable).thenReturn(true)
        whenever(exoPlayer.currentPosition).thenReturn(42L)
        player.release()
        initPlayer()


        player.play(SAMPLE_URI)


        verify(exoPlayer).prepare(hlsMediaSource, false, false)
    }

    companion object {
        const val SAMPLE_URI = "https://mycujoo.tv"
    }
}