package tv.mycujoo.mcls.manager

import android.animation.ObjectAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.idling.CountingIdlingResource
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.widgets.ScaffoldView
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewHandlerTest {

    private lateinit var viewHandler: IViewHandler

    @Mock
    lateinit var overLayHost: ConstraintLayout

    @Mock
    lateinit var view0: ScaffoldView

    @Mock
    lateinit var view1: ScaffoldView

    @Mock
    lateinit var animation0: ObjectAnimator

    @Mock
    lateinit var animation1: ObjectAnimator

    @Mock
    lateinit var idlingResource: CountingIdlingResource

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewHandler = ViewHandler(idlingResource)
        viewHandler.setOverlayHost(overLayHost)
    }


    @Test
    fun `given attachOverlayView should save view in list`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)


        viewHandler.attachOverlayView(view0)


        assertEquals(view0, viewHandler.getOverlayView(SAMPLE_TAG))
    }

    @Test
    fun `given duplicate attachOverlayView should not save it in list`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        Mockito.`when`(view1.tag).thenReturn(SAMPLE_TAG)
        viewHandler.attachOverlayView(view0)


        viewHandler.attachOverlayView(view1)


        assertEquals(view0, viewHandler.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given attachOverlayView with invalid tag, should not, save view in list`() {
        viewHandler.attachOverlayView(view0)


        assertEquals(null, viewHandler.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given request to remove existing view, should remove it`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewHandler.attachOverlayView(view0)


        viewHandler.detachOverlayView(view0)


        assertNull(viewHandler.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given request to remove invalid view, should do nothing`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewHandler.attachOverlayView(view0)


        viewHandler.detachOverlayView(null)
        viewHandler.detachOverlayView(view1)


        assertEquals(view0, viewHandler.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given attached overlay object, should return true for isAttached()`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewHandler.attachOverlayView(view0)

        assert(viewHandler.overlayIsAttached(SAMPLE_TAG))
    }

    @Test
    fun `given attached overlay object, should return false for isAttached()`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewHandler.attachOverlayView(view0)

        assertFalse(viewHandler.overlayIsNotAttached(SAMPLE_TAG))
    }

    @Test
    fun `given detached overlay object, should return false for isAttached()`() {
        assertFalse(viewHandler.overlayIsAttached(SAMPLE_TAG))
    }

    @Test
    fun `given detached overlay object, should return true for isNotAttached()`() {
        assertTrue(viewHandler.overlayIsNotAttached(SAMPLE_TAG))
    }

    @Test
    fun `given clear(), should clear all lists`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        Mockito.`when`(view1.tag).thenReturn(SAMPLE_TAG)
        viewHandler.attachOverlayView(view0)
        viewHandler.attachOverlayView(view1)


        viewHandler.clearAll()


        assertNull(viewHandler.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given addAnimation() should save animation in list`() {
        viewHandler.addAnimation(SAMPLE_TAG, animation0)


        assertEquals(animation0, viewHandler.getAnimationWithTag(SAMPLE_TAG))
    }

    @Test
    fun `given removeAnimation() should remove animation from list`() {
        viewHandler.addAnimation(SAMPLE_TAG, animation0)


        viewHandler.removeAnimation(SAMPLE_TAG)


        assertNull(viewHandler.getAnimationWithTag(SAMPLE_TAG))
    }

    @Test
    fun `given multiple addAnimation() should save all animatiosn in list`() {
        viewHandler.addAnimation(SAMPLE_TAG, animation0)
        viewHandler.addAnimation(SAMPLE_TAG, animation1)


        assertEquals(2, viewHandler.getAnimations().size)
    }

    companion object {
        const val SAMPLE_TAG = "tag_1000"
    }
}