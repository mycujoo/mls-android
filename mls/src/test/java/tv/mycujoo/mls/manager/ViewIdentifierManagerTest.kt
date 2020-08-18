package tv.mycujoo.mls.manager

import android.animation.ObjectAnimator
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import tv.mycujoo.mls.widgets.ScaffoldView
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ViewIdentifierManagerTest {

    lateinit var viewIdentifierManager: ViewIdentifierManager

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

    @Mock
    lateinit var dispatcher: CoroutineScope

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewIdentifierManager = ViewIdentifierManager(dispatcher, idlingResource)
    }


    @Test
    fun `given attachOverlayView should save view in list`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)


        viewIdentifierManager.attachOverlayView(view0)


        assertEquals(view0, viewIdentifierManager.getOverlayView(SAMPLE_TAG))
    }

    @Test
    fun `given duplicate attachOverlayView should not save it in list`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        Mockito.`when`(view1.tag).thenReturn(SAMPLE_TAG)
        viewIdentifierManager.attachOverlayView(view0)


        viewIdentifierManager.attachOverlayView(view1)


        assertEquals(view0, viewIdentifierManager.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given attachOverlayView with invalid tag, should not, save view in list`() {
        viewIdentifierManager.attachOverlayView(view0)


        assertEquals(null, viewIdentifierManager.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given request to remove existing view, should remove it`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewIdentifierManager.attachOverlayView(view0)


        viewIdentifierManager.detachOverlayView(view0)


        assertNull(viewIdentifierManager.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given request to remove invalid view, should do nothing`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewIdentifierManager.attachOverlayView(view0)


        viewIdentifierManager.detachOverlayView(null)
        viewIdentifierManager.detachOverlayView(view1)


        assertEquals(view0, viewIdentifierManager.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given attached overlay object, should return true for isAttached()`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewIdentifierManager.attachOverlayView(view0)

        assert(viewIdentifierManager.overlayBlueprintIsAttached(SAMPLE_TAG))
    }

    @Test
    fun `given attached overlay object, should return false for isAttached()`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        viewIdentifierManager.attachOverlayView(view0)

        assertFalse(viewIdentifierManager.overlayBlueprintIsNotAttached(SAMPLE_TAG))
    }

    @Test
    fun `given detached overlay object, should return false for isAttached()`() {
        assertFalse(viewIdentifierManager.overlayBlueprintIsAttached(SAMPLE_TAG))
    }

    @Test
    fun `given detached overlay object, should return true for isNotAttached()`() {
        assertTrue(viewIdentifierManager.overlayBlueprintIsNotAttached(SAMPLE_TAG))
    }

    @Test
    fun `given clear(), should clear all lists`() {
        Mockito.`when`(view0.tag).thenReturn(SAMPLE_TAG)
        Mockito.`when`(view1.tag).thenReturn(SAMPLE_TAG)
        viewIdentifierManager.attachOverlayView(view0)
        viewIdentifierManager.attachOverlayView(view1)


        viewIdentifierManager.clearAll()


        assertNull(viewIdentifierManager.getOverlayView(SAMPLE_TAG))
    }


    @Test
    fun `given addAnimation() should save animation in list`() {
        viewIdentifierManager.addAnimation(SAMPLE_TAG, animation0)


        assertEquals(animation0, viewIdentifierManager.getAnimationWithTag(SAMPLE_TAG))
    }

    @Test
    fun `given removeAnimation() should remove animation from list`() {
        viewIdentifierManager.addAnimation(SAMPLE_TAG, animation0)


        viewIdentifierManager.removeAnimation(SAMPLE_TAG)


        assertNull(viewIdentifierManager.getAnimationWithTag(SAMPLE_TAG))
    }

    @Test
    fun `given multiple addAnimation() should save all animatiosn in list`() {
        viewIdentifierManager.addAnimation(SAMPLE_TAG, animation0)
        viewIdentifierManager.addAnimation(SAMPLE_TAG, animation1)


        assertEquals(2, viewIdentifierManager.getAnimations().size)
    }

    companion object {
        const val SAMPLE_TAG = "tag_1000"
    }
}