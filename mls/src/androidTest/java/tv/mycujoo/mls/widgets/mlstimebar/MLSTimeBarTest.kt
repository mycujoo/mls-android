package tv.mycujoo.mls.widgets.mlstimebar

import android.content.Intent
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.core.view.MotionEventBuilder
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import tv.mycujoo.fake.FakeTimelineMarkerPosition
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R

class MLSTimeBarTest {


    /**region Subject under test*/
    private lateinit var mlsTimeBar: MLSTimeBar

    /**endregion */

    /**region Fields*/
    private val timeLineMarkerPosition = FakeTimelineMarkerPosition()

    /**endregion */


    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)

            mlsTimeBar = MLSTimeBar(activity)
            frameLayout.addView(mlsTimeBar)

            mlsTimeBar.setTimelineMarkerPositionListener(timeLineMarkerPosition)

        }
    }


    @Test
    fun seekingShouldCallPOIListener() {
        mlsTimeBar.setPosition(123L)


        assertEquals(123L, timeLineMarkerPosition.position)
    }


    @Ignore
    @Test
    fun scrubbingShouldCallPOIListener() {
        // Time-bar bounds on screen -> Rect(0, 1293 - 1440, 1371)
        mlsTimeBar.setDuration(120000)
        val motionEventStart =
            MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_DOWN).setPointer(1F, 1400F).build()
        val motionEventEnd =
            MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_MOVE).setPointer(50F, 1400F).build()


        UiThreadStatement.runOnUiThread {
            mlsTimeBar.onTouchEvent(motionEventStart)
            mlsTimeBar.onTouchEvent(motionEventEnd)

        }



        assertEquals(123L, timeLineMarkerPosition.position)
    }


}