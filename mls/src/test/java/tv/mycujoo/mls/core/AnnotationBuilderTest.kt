package tv.mycujoo.mls.core


import com.nhaarman.mockitokotlin2.argForWhich
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.helper.TestDataHelper.Companion.getAnnotationDataSource
import kotlin.test.assertNotNull

class AnnotationBuilderTest {

    lateinit var builder: AnnotationBuilder

    @Mock
    lateinit var publisher: AnnotationPublisher

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        builder = AnnotationBuilderImpl(publisher)
    }

    @Test
    fun `should have an AnnotationPublisher reference`() {
        assertNotNull(AnnotationBuilderImpl(AnnotationPublisherImpl()))
    }

    @Test
    fun `should be able to generate new Annotation`() {
        val annotationDataSource = getAnnotationDataSource()

        builder.buildAnnotation(annotationDataSource)

        Mockito.verify(publisher)
            .onNewAnnotationAvailable(argForWhich { (type == annotationDataSource.type && overlayData == annotationDataSource.overlayData) })
    }
}