package tv.mycujoo.mls.api

import com.google.android.exoplayer2.SimpleExoPlayer
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.core.VideoPlayerCoordinator

class VideoPlayerTest {

    private lateinit var videoPlayer: VideoPlayer

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var videoPlayerCoordinator: VideoPlayerCoordinator

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        videoPlayer = VideoPlayer(exoPlayer, videoPlayerCoordinator)
    }

    @Test
    fun `given request to play event with id, should call videoPlayerCoordinator`() {
        videoPlayer.playVideo("42")

        verify(videoPlayerCoordinator).playVideo("42")
    }

    @Test
    fun `given request to play event with eventEntity, should call videoPlayerCoordinator`() {
        val event = getSampleEventEntity()
        videoPlayer.playVideo(event)

        verify(videoPlayerCoordinator).playVideo(event)
    }

    private fun getSampleEventEntity(): EventEntity {
        val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
        return EventEntity(
            "42",
            "",
            "",
            "",
            location,
            "",
            "",
            "",
            emptyList(),
            "",
            emptyList(),
            Metadata(),
            false
        )
    }


}