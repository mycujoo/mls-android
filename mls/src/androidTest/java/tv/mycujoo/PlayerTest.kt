package tv.mycujoo

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.MLSPlayerView


@RunWith(AndroidJUnit4::class)
class PlayerTest {

    private lateinit var mlsPlayerView: MLSPlayerView
    private var countingIdlingResource = CountingIdlingResource("MLSPlayerView")


    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val linearLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            mlsPlayerView = MLSPlayerView(linearLayout.context)
            mlsPlayerView.id = View.generateViewId()
            linearLayout.addView(mlsPlayerView)
            mlsPlayerView.idlingResource = countingIdlingResource
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

    @Ignore
    @Test
    fun checkIfViewIsDisplayed() {
        onView(withId(mlsPlayerView.id)).check(matches(isDisplayed()))
    }

    @Test
    fun displayEventInfo() {
        mlsPlayerView.setEventInfo(EVENT_TITLE, EVENT_DESCRIPTION, EVENT_DATE)


        UiThreadStatement.runOnUiThread {
            mlsPlayerView.showEventInfoForStartedEvents()
        }


        onView(withText(EVENT_TITLE)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText(EVENT_DESCRIPTION)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun hideEventInfo() {
        mlsPlayerView.setEventInfo(EVENT_TITLE, EVENT_DESCRIPTION, EVENT_DATE)
        UiThreadStatement.runOnUiThread {
            mlsPlayerView.showEventInfoForStartedEvents()
        }
        onView(withText(EVENT_TITLE)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))


        UiThreadStatement.runOnUiThread {
            mlsPlayerView.hideInfoDialogs()
        }


        onView(withText(EVENT_TITLE)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withText(EVENT_DESCRIPTION)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    companion object {
        const val EVENT_TITLE = "event_title"
        const val EVENT_DESCRIPTION = "event_desc"
        const val EVENT_DATE = "2020-07-11T07:32:46Z"
    }
}