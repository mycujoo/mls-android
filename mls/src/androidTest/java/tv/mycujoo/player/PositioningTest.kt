package tv.mycujoo.player

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
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
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.getShowOverlayActionEntity
import tv.mycujoo.matchers.HorizontalBiasMatcher
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.matchers.VerticalBiasMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.ProportionalImageView


@RunWith(AndroidJUnit4::class)
class PositioningTest {

    private lateinit var playerViewWrapper: PlayerViewWrapper

    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
            .putExtra("key", "value")
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            playerViewWrapper = PlayerViewWrapper(frameLayout.context)
            playerViewWrapper.id = View.generateViewId()
            frameLayout.addView(playerViewWrapper)
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

    /**region New Annotation Structure*/
    @Test
    fun givenHCenterInPositionGuide_shouldPositionXWise() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(hCenter = 0F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                HorizontalBiasMatcher(0.5F)
            )
        )
    }

    @Test
    fun givenHCenterInPositionGuide_shouldPositionXWise_2() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(hCenter = 50F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                HorizontalBiasMatcher(1F)
            )
        )
    }

    @Test
    fun givenHCenterInPositionGuide_shouldPositionXWise_3() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(hCenter = 40F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                HorizontalBiasMatcher(0.9F)
            )
        )
    }

    @Test
    fun givenHCenterInPositionGuide_shouldPositionXWise_4() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(hCenter = -50F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                HorizontalBiasMatcher(0F)
            )
        )
    }

    @Test
    fun givenHCenterInPositionGuide_shouldPositionXWise_5() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(hCenter = -40F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                HorizontalBiasMatcher(0.1F)
            )
        )
    }

    @Test
    fun givenVCenterInPositionGuide_shouldPositionYWise() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(vCenter = 0F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                VerticalBiasMatcher(0.5F)
            )
        )
    }

    @Test
    fun givenVCenterInPositionGuide_shouldPositionYWise_1() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(vCenter = 50F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                VerticalBiasMatcher(1F)
            )
        )
    }

    @Test
    fun givenVCenterInPositionGuide_shouldPositionYWise_closeToBottom() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(vCenter = 40F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                VerticalBiasMatcher(0.9F)
            )
        )
    }

    @Test
    fun givenVCenterInPositionGuide_shouldPositionYWise_exactlyAtTop() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(vCenter = -50F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                VerticalBiasMatcher(0F)
            )
        )
    }

    @Test
    fun givenVCenterInPositionGuide_shouldPositionYWise_closeToTop() {
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            doesNotExist()
        )
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.showOverlay(
                getShowOverlayActionEntity(
                    1000L,
                    PositionGuide(vCenter = -40F)
                )
            )
        }
        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )


        onView(withClassName(TypeMatcher(ProportionalImageView::class.java.canonicalName))).check(
            matches(
                VerticalBiasMatcher(0.1F)
            )
        )
    }

    /**endregion */
}