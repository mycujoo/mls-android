package tv.mycujoo.mls.widgets.mlstimebar

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R

@RunWith(AndroidJUnit4::class)
class TimelineMarkerViewTest {

    private lateinit var timelineMarkerView: TimelineMarkerView

    private var countingIdlingResource = CountingIdlingResource("ViewHandler")

    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)

            val constraintLayout = ConstraintLayout(activity)
            val activityLayoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            frameLayout.addView(constraintLayout, activityLayoutParams)

            val parentLayout = ConstraintLayout(activity)
            parentLayout.id = View.generateViewId()
            val parentLayoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            constraintLayout.addView(parentLayout, parentLayoutParams)


            timelineMarkerView = TimelineMarkerView(activity)
            timelineMarkerView.id = View.generateViewId()
            timelineMarkerView.setIdlingResource(countingIdlingResource)
            parentLayout.addView(timelineMarkerView)
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(countingIdlingResource)
    }

    @Test
    fun initializingShouldSetColor() {
        val color = "#55FFFF"


        timelineMarkerView.initialize(color)


        assertEquals(color, timelineMarkerView.bgColor)
    }

    @Test
    fun positionLessThanScreenWidthShouldConstraintMarkerToLeftOfParent() {
        UiThreadStatement.runOnUiThread {
            val position = (timelineMarkerView.parent as View).width - (timelineMarkerView.measuredWidth / 2)
            timelineMarkerView.setMarkerTexts(sampleTitleList(), position)
        }

        waitForIdlingResourceCounter(true)
        assertTrue(-1 != (timelineMarkerView.layoutParams as ConstraintLayout.LayoutParams).startToStart)
        assertTrue(-1 == (timelineMarkerView.layoutParams as ConstraintLayout.LayoutParams).endToEnd)
    }

    @Test
    fun positionLessThanScreenWidthShouldAddMarginLeftToMarker() {
        UiThreadStatement.runOnUiThread {
            val position = (timelineMarkerView.parent as View).width - (timelineMarkerView.width / 2)
            timelineMarkerView.setMarkerTexts(sampleTitleList(), position - 1)
        }


        waitForIdlingResourceCounter(true)
        assertTrue(
            -1 != timelineMarkerView.marginLeft
        )
    }


    @Test
    fun positionLessThanScreenWidthShouldNotPlaceMarkerAtRightOfParent() {
        UiThreadStatement.runOnUiThread {
            val position = (timelineMarkerView.parent as View).width - (timelineMarkerView.measuredWidth / 2)
            timelineMarkerView.setMarkerTexts(sampleTitleList(), position)
        }


        waitForIdlingResourceCounter(true)
        assertTrue(-1 == (timelineMarkerView.layoutParams as ConstraintLayout.LayoutParams).endToEnd)
    }

    @Test
    fun positionGreaterThanScreenWidthShouldPlaceMarkerAtRightOfParent() {
        UiThreadStatement.runOnUiThread {
            val position = (timelineMarkerView.parent as View).width - (timelineMarkerView.measuredWidth / 2)
            timelineMarkerView.setMarkerTexts(sampleTitleList(), position+1)
        }

        waitForIdlingResourceCounter(true)
        assertTrue(-1 != (timelineMarkerView.layoutParams as ConstraintLayout.LayoutParams).endToEnd)
    }

    @Test
    fun positionGreaterThanScreenWidthShouldNotConstraintMarkerToLeftOfParent() {
        UiThreadStatement.runOnUiThread {
            val position = (timelineMarkerView.parent as View).width - (timelineMarkerView.measuredWidth / 2)
            timelineMarkerView.setMarkerTexts(sampleTitleList(), position+1)
        }

        waitForIdlingResourceCounter(true)
        assertTrue(-1 == (timelineMarkerView.layoutParams as ConstraintLayout.LayoutParams).startToStart)
    }

    @Test
    fun positionGreaterThanScreenWidthShouldNotAddMarginLeftToMarker() {
        UiThreadStatement.runOnUiThread {
            val position = (timelineMarkerView.parent as View).width - (timelineMarkerView.measuredWidth / 2)
            timelineMarkerView.setMarkerTexts(sampleTitleList(), position+1)
        }

        waitForIdlingResourceCounter(true)
        assertTrue(-1 == timelineMarkerView.marginLeft)
    }


    @Test
    fun removeMarkerTextsShouldRemoveAllChildViews() {
        assertEquals(0, timelineMarkerView.childCount)
        val titleList = listOf("Goal", "Foul")
        UiThreadStatement.runOnUiThread {
            timelineMarkerView.setMarkerTexts(titleList, 0)
        }
        waitForIdlingResourceCounter(true)
        assertEquals(2, timelineMarkerView.childCount)


        timelineMarkerView.removeMarkerTexts()

        waitForIdlingResourceCounter(false)
        assertEquals(0, timelineMarkerView.childCount)
    }

    @Test
    fun givenListOfMarkerTitles_shouldAddThem() {
        val titleList = listOf("Goal", "Foul")


        UiThreadStatement.runOnUiThread {
            timelineMarkerView.setMarkerTexts(titleList, 0)
        }
//        UiThreadStatement.runOnUiThread {
//            timelineMarkerView.setPosition(30)
//        }


        onView(withText(titleList[0])).check(
            ViewAssertions.matches(isDisplayed())
        )
        onView(withText(titleList[1])).check(
            ViewAssertions.matches(isDisplayed())
        )
    }

    private fun sampleTitleList() : List<String>{
        return listOf("Goal")
    }


    /**region Helper*/
    private fun waitForIdlingResourceCounter(isVisible: Boolean) {
        if (isVisible) {
            onView(withId(timelineMarkerView.id)).check(
                ViewAssertions.matches(isDisplayed())
            )
        } else {
            onView(withId(timelineMarkerView.id)).check(
                ViewAssertions.matches(withEffectiveVisibility(Visibility.INVISIBLE))
            )
        }
    }
    /**endregion */
}