package tv.mycujoo

import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.widgets.PlayerViewWrapper


@RunWith(AndroidJUnit4::class)
class PlayerTest {

    private lateinit var playerViewWrapper: PlayerViewWrapper

    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
            .putExtra("key", "value")
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            // do some stuff with the Activity
            println("RUNNING")
            val linearLayout = activity.findViewById<LinearLayout>(R.id.blankActivity_rootView)
            playerViewWrapper = PlayerViewWrapper(linearLayout.context)
            playerViewWrapper.id = View.generateViewId()
            linearLayout.addView(playerViewWrapper)

        }

    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(playerViewWrapper.idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(playerViewWrapper.idlingResource)
    }

    @Test
    fun checkIfViewIsDisplayed() {
        onView(withId(playerViewWrapper.id)).check(matches(isDisplayed()))
    }


    @Test
    fun givenAnnouncementOverlayAction_shouldDisplayIt() {
        onView(withText("Line 1")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showAnnouncementOverLay(getSampleShowAnnouncementOverlayAction())
        }

        onView(withText("Line 1")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }


    @Test
    fun givenDismissibleAnnouncementOverlayAction_shouldRemoveAfterDisplayingIt() {
        onView(withText("Line 1")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
            val action = getSampleShowAnnouncementOverlayAction()
            action.dismissible = true
            action.dismissIn = 200L
            playerViewWrapper.showAnnouncementOverLay(action)
        }

        onView(withText("Line 1")).check(doesNotExist())
    }

    @Test
    fun givenScoreboardOverlayAction_shouldDisplayIt() {
        onView(withText("FCB")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showScoreboardOverlay(getSampleShowScoreboardAction())
        }

        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun givenDismissibleScoreboardOverlayAction_shouldRemoveAfterDisplayingIt() {
        onView(withText("FCB")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
            val action = getSampleShowScoreboardAction()
            action.dismissible = true
            action.dismissIn = 200L
            playerViewWrapper.showScoreboardOverlay(action)
        }

        onView(withText("FCB")).check(doesNotExist())
    }


    companion object {
        private fun getSampleShowAnnouncementOverlayAction(): ShowAnnouncementOverlayAction {
            val showAnnouncementOverlayAction = ShowAnnouncementOverlayAction()
            showAnnouncementOverlayAction.color = "#cccccc"
            showAnnouncementOverlayAction.line1 = "Line 1"
            showAnnouncementOverlayAction.line2 = " Line 2"
            showAnnouncementOverlayAction.imageUrl = "some url"

            showAnnouncementOverlayAction.viewId = "action_view_id_10000"

            return showAnnouncementOverlayAction
        }

        private fun getSampleShowScoreboardAction(): ShowScoreboardOverlayAction {
            val showScoreboardOverlayAction = ShowScoreboardOverlayAction()
            showScoreboardOverlayAction.colorLeft = "#cccccc"
            showScoreboardOverlayAction.colorRight = "#f4f4f4"
            showScoreboardOverlayAction.abbrLeft = "FCB"
            showScoreboardOverlayAction.abbrRight = " CFC"
            showScoreboardOverlayAction.scoreLeft = "0"
            showScoreboardOverlayAction.scoreRight = "0"

            showScoreboardOverlayAction.viewId = "action_view_id_10001"

            return showScoreboardOverlayAction
        }
    }
}