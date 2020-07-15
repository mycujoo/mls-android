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
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.matchers.ViewSizeMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.ProportionalImageView


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

    /**region New Annotation Structure*/
    @Test
    fun givenShowOverlayAction_shouldDisplayIt() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showOverlay(getShowOverlayActionEntity(1000L))
        }

        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )
    }


    @Test
    fun givenShowOverlayAction_withFullWidth_shouldDisplayInFullWidth() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showOverlay(getShowOverlayActionEntity(1000L))
        }

        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                ViewSizeMatcher(300)
            )
        )
    }

    @Test
    fun giveHideOverlayAction_shouldHideRelatedOverlay() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showOverlay(getShowOverlayActionEntity(1000L))
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.hideOverlay(getHideOverlayActionEntity(15000L))
        }

        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
    }

    /**endregion */

    @Test
    fun givenAnnouncementOverlayAction_shouldDisplayIt() {
        onView(withText("Line 1")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showAnnouncementOverLay(getSampleShowAnnouncementOverlayAction())
        }

        onView(withText("Line 1")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }


    @Test
    fun givenDismissibleAnnouncementOverlayAction_shouldRemoveAfterDisplayingIt() {
        onView(withText("Line 1")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowAnnouncementOverlayAction()
//            action.dismissible = true
//            action.dismissIn = 200L
//            playerViewWrapper.showAnnouncementOverLay(action)
        }

        onView(withText("Line 1")).check(doesNotExist())
    }

    @Test
    fun givenScoreboardOverlayAction_shouldDisplayIt() {
        onView(withText("FCB")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showScoreboardOverlay(getSampleShowScoreboardAction())
        }

        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun givenDismissibleScoreboardOverlayAction_shouldHideAfterDisplayingIt() {
        onView(withText("FCB")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowScoreboardAction()
//            action.dismissible = true
//            playerViewWrapper.showScoreboardOverlay(action)
//            playerViewWrapper.hideOverlay(action.viewId)
        }

        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }

    @Test
    fun givenDismissibleScoreboardOverlayAction_shouldRemoveAfterDisplayingIt() {
        onView(withText("FCB")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowScoreboardAction()
//            action.dismissible = true
//            playerViewWrapper.showScoreboardOverlay(action)
//            playerViewWrapper.removeOverlay(action.viewId)
        }

        onView(withText("FCB")).check(doesNotExist())
    }

    @Test
    fun givenRemoveCommandShouldRemoveTarget() {
        onView(withText("FCB")).check(doesNotExist())
        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowScoreboardAction()
//            playerViewWrapper.showScoreboardOverlay(action)
        }
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))


//        playerViewWrapper.executeCommand(getSampleCommandAction("remove"))
        onView(withText("FCB")).check(doesNotExist())
    }

    @Test
    fun givenHideCommandShouldHideTarget() {
        onView(withText("FCB")).check(doesNotExist())
        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowScoreboardAction()
//            playerViewWrapper.showScoreboardOverlay(action)
        }
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))


//        playerViewWrapper.executeCommand(getSampleCommandAction("hide"))
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }

    @Test
    fun givenShowCommandShouldShowTarget() {
        onView(withText("FCB")).check(doesNotExist())
        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowScoreboardAction()
//            playerViewWrapper.showScoreboardOverlay(action)
        }
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
//        playerViewWrapper.executeCommand(getSampleCommandAction("hide"))
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))


//        playerViewWrapper.executeCommand(getSampleCommandAction("show"))
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Ignore
    @Test
    fun givenSeekToCommandShouldRemoveAllPreviousOverlays() {
        onView(withText("FCB")).check(doesNotExist())
        UiThreadStatement.runOnUiThread {
//            val action = getSampleShowScoreboardAction_WithDismissingParams()
//            playerViewWrapper.showScoreboardOverlay(action)
        }
        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))


//        onView(withText("FCB")).check(doesNotExist())
    }


    companion object {
//        private fun getSampleShowAnnouncementOverlayAction(): ShowAnnouncementOverlayAction {
//            val showAnnouncementOverlayAction = ShowAnnouncementOverlayAction()
//            showAnnouncementOverlayAction.color = "#cccccc"
//            showAnnouncementOverlayAction.line1 = "Line 1"
//            showAnnouncementOverlayAction.line2 = " Line 2"
//            showAnnouncementOverlayAction.imageUrl = "some url"
//
//            showAnnouncementOverlayAction.viewId = "action_view_id_10000"
//
//            return showAnnouncementOverlayAction
//        }
//
//        private fun getSampleShowScoreboardAction(): ShowScoreboardOverlayAction {
//            val showScoreboardOverlayAction = ShowScoreboardOverlayAction()
//            showScoreboardOverlayAction.colorLeft = "#cccccc"
//            showScoreboardOverlayAction.colorRight = "#f4f4f4"
//            showScoreboardOverlayAction.abbrLeft = "FCB"
//            showScoreboardOverlayAction.abbrRight = " CFC"
//            showScoreboardOverlayAction.scoreLeft = "0"
//            showScoreboardOverlayAction.scoreRight = "0"
//
//            showScoreboardOverlayAction.viewId = "action_view_id_10001"
//
//            return showScoreboardOverlayAction
//        }
//
//        private fun getSampleShowScoreboardAction_WithDismissingParams(): ShowScoreboardOverlayAction {
//            val showScoreboardOverlayAction = ShowScoreboardOverlayAction()
//            showScoreboardOverlayAction.colorLeft = "#cccccc"
//            showScoreboardOverlayAction.colorRight = "#f4f4f4"
//            showScoreboardOverlayAction.abbrLeft = "FCB"
//            showScoreboardOverlayAction.abbrRight = " CFC"
//            showScoreboardOverlayAction.scoreLeft = "0"
//            showScoreboardOverlayAction.scoreRight = "0"
//
//            showScoreboardOverlayAction.viewId = "action_view_id_10001"
//            showScoreboardOverlayAction.dismissible = true
//            showScoreboardOverlayAction.dismissIn = 3000L
//
//            return showScoreboardOverlayAction
//        }
//
//        private fun getSampleCommandAction(verb: String): CommandAction {
//            val commandAction = CommandAction()
//            commandAction.verb = verb
//            commandAction.targetViewId = "action_view_id_10001"
//            commandAction.offset = 100L
//
//            return commandAction
//        }
    }
}