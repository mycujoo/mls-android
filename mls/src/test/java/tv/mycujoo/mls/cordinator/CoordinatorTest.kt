package tv.mycujoo.mls.cordinator


import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.core.AnnotationPublisherImpl
import tv.mycujoo.mls.helper.TestDataHelper.Companion.getAnnotationBundle
import tv.mycujoo.mls.widgets.PlayerWidget
import kotlin.test.assertNotNull

 class CoordinatorTest {

    lateinit var coordinator: Coordinator

    @Mock
    lateinit var widget: PlayerWidget
    private var publisher= AnnotationPublisherImpl()

     @Before
     fun setUp() {
        MockitoAnnotations.initMocks(this)
        coordinator = Coordinator(widget, publisher)

    }

    @Test
    fun `should init with PlayerWidget and AnnotationPublisher`() {
        assertNotNull(Coordinator(widget, publisher))
    }


    @Test
    fun `given new Annotation, should call widget to display it`() {
        val annotationBundle = getAnnotationBundle()

        publisher.onNewAnnotationAvailable(annotationBundle)

        Mockito.verify(widget).displayAnnotation(annotationBundle)
    }
}