package tv.mycujoo.mls.cordinator


import android.os.Handler
import com.google.android.exoplayer2.SimpleExoPlayer
import com.nhaarman.mockitokotlin2.anyVararg
import com.nhaarman.mockitokotlin2.eq
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.AnnotationPublisherImpl
import tv.mycujoo.mls.helper.TestDataHelper.Companion.getAnnotationBundle
import tv.mycujoo.mls.model.AnnotationBundle
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.PlayerWidget
import kotlin.test.assertNotNull

class CoordinatorTest {

    lateinit var coordinator: Coordinator

    @Mock
    lateinit var api: Api

    @Mock
    lateinit var widget: PlayerWidget
    private var publisher = AnnotationPublisherImpl()

    @Mock
    lateinit var exoPlayer: SimpleExoPlayer

    @Mock
    lateinit var handler: Handler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        coordinator = Coordinator(api, publisher)
        coordinator.widget = widget

        val listener = object : AnnotationListener {
            override fun onNewAnnotationAvailable(annotationBundle: AnnotationBundle) {
                widget.displayAnnotation(annotationBundle)
            }
        }
        publisher.setAnnotationListener(listener)

    }

    @Test
    fun `should set PlayerWidget`() {
        val coordinator = Coordinator(api, publisher)
        coordinator.widget = widget
        assertNotNull(coordinator.widget)

    }

    @Test
    fun `initialize should fetch annotation from Api`() {
        coordinator.initialize(exoPlayer, handler)

        Mockito.verify(api).getAnnotations()
    }

    @Test
    fun `initialize should run a runnable with a heart-beat`() {
        coordinator.initialize(exoPlayer, handler)

        Mockito.verify(handler).postDelayed(anyVararg(), eq(1000L))
    }


    @Test
    fun `given new Annotation, should call widget to display it`() {
        val annotationBundle = getAnnotationBundle()

        publisher.onNewAnnotationAvailable(annotationBundle)

        Mockito.verify(widget).displayAnnotation(annotationBundle)
    }
}