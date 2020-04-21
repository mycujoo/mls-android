package tv.mycujoo.mls.core

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.helper.TestDataHelper.Companion.getAnnotationBundle

internal class AnnotationPublisherImplTest {

    lateinit var publisher: AnnotationPublisher

    @Mock
    lateinit var listener: AnnotationListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        publisher = AnnotationPublisherImpl()
        publisher.setAnnotationListener(listener)
    }


    @Test
    internal fun `on new annotation received, should call listeners passing the annotations`() {
        val annotationBundle = getAnnotationBundle()
        publisher.onNewAnnotationAvailable(annotationBundle)

        Mockito.verify(listener).onNewAnnotationAvailable(annotationBundle)
    }


}