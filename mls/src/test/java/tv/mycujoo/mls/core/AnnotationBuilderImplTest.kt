package tv.mycujoo.mls.core

import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.*

class AnnotationBuilderImplTest {

    private lateinit var annotationBuilderImpl: AnnotationBuilderImpl


    @Mock
    lateinit var annotationListener: AnnotationListener

    @Mock
    lateinit var okHttpClient: OkHttpClient

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val svgData = SvgData(
            "https://storage.googleapis.com/mycujoo-player-app.appspot.com/scoreboard_and_timer.svg",
            null
        )
        val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_IN, 1000L)

    }


    @Test
    fun `regular play mode, add overlay with animation`() {
        annotationBuilderImpl.setCurrentTime(900L, true)
        annotationBuilderImpl.buildCurrentTimeRange()
    }
}