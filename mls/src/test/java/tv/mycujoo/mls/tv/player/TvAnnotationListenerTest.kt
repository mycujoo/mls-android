package tv.mycujoo.mls.tv.player

import androidx.test.espresso.idling.CountingIdlingResource
import com.nhaarman.mockitokotlin2.any
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.domain.entity.ViewSpec
import tv.mycujoo.mls.helper.DownloaderClient

class TvAnnotationListenerTest {

    private lateinit var tvAnnotationListener: TvAnnotationListener

    @Mock
    lateinit var tvOverlayContainer: TvOverlayContainer

    @Mock
    lateinit var downloaderClient: DownloaderClient

    @Mock
    lateinit var coroutineScope: CoroutineScope

    @Mock
    lateinit var countingIdlingResource: CountingIdlingResource

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tvAnnotationListener = TvAnnotationListener(
            tvOverlayContainer,
            coroutineScope,
            CountingIdlingResource("ViewIdentifierManager"),
            downloaderClient
        )
    }

    @Test
    fun addOverlay() {
        val overlayEntity = OverlayEntity(
            "id_0",
            null,
            ViewSpec(null, null),
            TransitionSpec(0L, AnimationType.NONE, 0L),
            TransitionSpec(-1L, AnimationType.NONE, -1L),
            emptyList()
        )
        Mockito.`when`(downloaderClient.download(any(), any()))
            .then { i -> ((i.getArgument(1)) as (OverlayEntity) -> Unit).invoke(i.getArgument(0)) }


        tvAnnotationListener.addOverlay(overlayEntity)


        Mockito.verify(tvOverlayContainer).addOverlay(overlayEntity)
    }

    @Test
    fun removeOverlay() {
        val overlayEntity = OverlayEntity(
            "id_0",
            null,
            ViewSpec(null, null),
            TransitionSpec(0L, AnimationType.NONE, 0L),
            TransitionSpec(-1L, AnimationType.NONE, -1L),
            emptyList()
        )


        tvAnnotationListener.removeOverlay(overlayEntity)


        Mockito.verify(tvOverlayContainer).removeOverlay(overlayEntity)

    }
}