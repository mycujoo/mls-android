package tv.mycujoo.mcls.widgets.mlstimebar

import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.test.core.view.MotionEventBuilder
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.fake.FakeTimelineMarkerPosition
import tv.mycujoo.mcls.BlankActivity
import tv.mycujoo.mcls.R

@RunWith(AndroidJUnit4::class)
class MLSTimeBarTest {

    /**
     * Rules
     */
    @get:Rule
    var activityScenarioRule = activityScenarioRule<BlankActivity>()

    /**region Subject under test*/
    private lateinit var mlsTimeBar: MLSTimeBar

    /**endregion */

    /**region Fields*/
    private val timeLineMarkerPosition = FakeTimelineMarkerPosition()

    /**endregion */


    @Before
    fun setUp() {
        activityScenarioRule.scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)

            mlsTimeBar = MLSTimeBar(activity)
            frameLayout.addView(mlsTimeBar)

            mlsTimeBar.setTimelineMarkerPositionListener(timeLineMarkerPosition)

        }
    }


    @Test
    fun scrubbingShouldCallPOIListener() {
        // Time-bar bounds on screen -> Rect(0, 1293 - 1440, 1371)

        activityScenarioRule.scenario.onActivity {
            mlsTimeBar.setDuration(120000)
            val frameHeight = it.findViewById<FrameLayout>(R.id.blankActivity_rootView).height

            val motionEventStart =
                MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_DOWN)
                    .setPointer(1F, (frameHeight / 2).toFloat())
                    .build()

            val motionEventEnd =
                MotionEventBuilder.newBuilder().setAction(MotionEvent.ACTION_MOVE)
                    .setPointer(50F, (frameHeight / 2).toFloat())
                    .build()

            mlsTimeBar.onTouchEvent(motionEventStart)
            mlsTimeBar.onTouchEvent(motionEventEnd)

            assertEquals(50L, timeLineMarkerPosition.position)
        }
    }


}