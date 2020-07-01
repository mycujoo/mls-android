package tv.mycujoo.mls.manager

import android.view.View
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class ViewIdentifierManagerTest {

    lateinit var viewIdentifierManager: ViewIdentifierManager

    @Mock
    lateinit var view0: View

    @Mock
    lateinit var view1: View

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewIdentifierManager = ViewIdentifierManager()
    }

    @Test
    fun storeAndGetSingleViewId() {
        Mockito.`when`(view0.id).thenReturn(10000)

        viewIdentifierManager.storeViewId(view0, "show_overlay_action")

        assertEquals(view0.id, viewIdentifierManager.getViewIdentifier("show_overlay_action"))
    }

    @Test
    fun storeAndGetMultipleViewId() {
        Mockito.`when`(view0.id).thenReturn(10000)
        Mockito.`when`(view1.id).thenReturn(10001)

        viewIdentifierManager.storeViewId(view0, "show_overlay_action")
        viewIdentifierManager.storeViewId(view1, "show_scoreboard_action")

        assertEquals(view0.id, viewIdentifierManager.getViewIdentifier("show_overlay_action"))
        assertEquals(view1.id, viewIdentifierManager.getViewIdentifier("show_scoreboard_action"))
    }
}