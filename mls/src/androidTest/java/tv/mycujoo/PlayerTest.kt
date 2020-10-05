package tv.mycujoo

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.matchers.ViewSizeMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.MLSPlayerView
import tv.mycujoo.mls.widgets.ProportionalImageView


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
            mlsPlayerView.displayEventInfoForStartedEvents()
        }


        onView(withText(EVENT_TITLE)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText(EVENT_DESCRIPTION)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun hideEventInfo() {
        mlsPlayerView.setEventInfo(EVENT_TITLE, EVENT_DESCRIPTION, EVENT_DATE)
        UiThreadStatement.runOnUiThread {
            mlsPlayerView.displayEventInfoForStartedEvents()
        }
        onView(withText(EVENT_TITLE)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))


        UiThreadStatement.runOnUiThread {
            mlsPlayerView.hideEventInfoDialog()
        }


        onView(withText(EVENT_TITLE)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withText(EVENT_DESCRIPTION)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    /**region New Annotation Structure*/
    @Ignore
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

    @Ignore
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

    @Ignore
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
    @Ignore
    @Test
    fun givenAnnouncementOverlayAction_shouldDisplayIt() {
        onView(withText("Line 1")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showAnnouncementOverLay(getSampleShowAnnouncementOverlayAction())
        }

        onView(withText("Line 1")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Ignore
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

    @Ignore
    @Test
    fun givenScoreboardOverlayAction_shouldDisplayIt() {
        onView(withText("FCB")).check(doesNotExist())

        UiThreadStatement.runOnUiThread {
//            playerViewWrapper.showScoreboardOverlay(getSampleShowScoreboardAction())
        }

        onView(withText("FCB")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Ignore
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
        const val EVENT_TITLE = "event_title"
        const val EVENT_DESCRIPTION = "event_desc"
        const val EVENT_DATE = "2020-07-11T07:32:46Z"
    }
}