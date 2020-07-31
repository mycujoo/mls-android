package tv.mycujoo.mls.helper

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.domain.entity.*
import tv.mycujoo.fake.FakeAnimationHelper
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.ScaffoldView
import tv.mycujoo.sampleSvgString

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class OverlayEntityViewHelperTest {

    private lateinit var playerViewWrapper: PlayerViewWrapper
    private var viewIdentifierManager = ViewIdentifierManager(GlobalScope)

    private var animationHelper = FakeAnimationHelper()


    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            playerViewWrapper = PlayerViewWrapper(frameLayout.context)
            playerViewWrapper.id = View.generateViewId()
            frameLayout.addView(playerViewWrapper)

            playerViewWrapper.idlingResource = viewIdentifierManager.idlingResource
            playerViewWrapper.prepare(OverlayViewHelper(animationHelper), viewIdentifierManager)
        }
    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(viewIdentifierManager.idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(viewIdentifierManager.idlingResource)
    }

    @Test
    fun ensureNoOverlayIsPreDrawn() {
        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.doesNotExist()
            )
    }

    @Test
    fun addOverlayWithNoAnimation_shouldAddOverlayView() {
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )
    }


    @Test
    fun addOverlayWithNoAnimation_shouldNotMakeAnimation() {
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())


        val animationRecipe = animationHelper.animationRecipe
        assertNull(animationRecipe)
    }

    @Test
    fun addOverlayWithAnimation_shouldAddOverlayView() {
        playerViewWrapper.onNewOverlayWithAnimation(getSampleOverlayEntity(AnimationType.FADE_IN))


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )
    }


    @Test
    fun addOverlayWithAnimation_shouldMakeStaticIntroAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.FADE_IN)
        playerViewWrapper.onNewOverlayWithAnimation(overlayEntity)


        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.introTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.introTransitionSpec.animationDuration, animationRecipe?.animationDuration)
    }

    @Test
    fun addOverlayWithAnimation_shouldMakeDynamicIntroAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.SLIDE_FROM_LEFT)
        playerViewWrapper.onNewOverlayWithAnimation(overlayEntity)

        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.introTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.introTransitionSpec.animationDuration, animationRecipe?.animationDuration)
    }


    private fun getSampleOverlayEntity(): OverlayEntity {
        val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))

        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 0L)
        val outroTransitionSpec = TransitionSpec(2000L, AnimationType.NONE, 0L)

        return OverlayEntity(
            "id_1001",
            null,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }

    private fun getSampleOverlayEntity(introAnimationType: AnimationType): OverlayEntity {
        val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))

        val svgData = SvgData(null, null, sampleSvgString)

        val introTransitionSpec = TransitionSpec(1000L, introAnimationType, 0L)
        val outroTransitionSpec = TransitionSpec(2000L, AnimationType.NONE, 0L)

        return OverlayEntity(
            "id_1001",
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }
}