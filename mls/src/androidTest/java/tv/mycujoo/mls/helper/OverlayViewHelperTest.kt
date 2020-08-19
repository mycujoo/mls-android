package tv.mycujoo.mls.helper

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
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
import tv.mycujoo.fake.FakeAnimationFactory
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.widgets.PlayerViewWrapper
import tv.mycujoo.mls.widgets.ScaffoldView
import tv.mycujoo.sampleSvgString

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class OverlayViewHelperTest {

    private lateinit var playerViewWrapper: PlayerViewWrapper
    private var viewIdentifierManager = ViewHandler(
        GlobalScope,
        CountingIdlingResource("ViewIdentifierManager")
    )

    private var animationHelper = FakeAnimationFactory()


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
            playerViewWrapper.prepare(
                OverlayViewHelper(animationHelper),
                viewIdentifierManager,
                emptyList()
            )
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

    @Test
    fun addOverlayWithWrongAnimation_shouldNotAddOverlayOrMakeAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.FADE_OUT)
        playerViewWrapper.onNewOverlayWithAnimation(overlayEntity)

        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.doesNotExist()
            )

        assertNull(animationHelper.animationRecipe)
    }


    @Test
    fun removeOverlayWithStaticAnimation_shouldMakeAnimation() {
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())
        val overlayEntity = getSampleOverlayEntity(AnimationType.UNSPECIFIED, AnimationType.FADE_OUT)


        playerViewWrapper.onOverlayRemovalWithAnimation(overlayEntity)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.outroTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.outroTransitionSpec.animationDuration, animationRecipe?.animationDuration)
    }

    @Test
    fun removeOverlayWithDynamicAnimation_shouldMakeAnimation() {
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())
        val overlayEntity = getSampleOverlayEntity(AnimationType.UNSPECIFIED, AnimationType.SLIDE_TO_LEFT)


        playerViewWrapper.onOverlayRemovalWithAnimation(overlayEntity)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.outroTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.outroTransitionSpec.animationDuration, animationRecipe?.animationDuration)
    }

    @Test
    fun removeOverlayWithWrongAnimation_shouldNotMakeAnimation() {
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())
        val overlayEntity = getSampleOverlayEntity(AnimationType.UNSPECIFIED, AnimationType.FADE_IN)


        playerViewWrapper.onOverlayRemovalWithAnimation(overlayEntity)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        assertNull(animationHelper.animationRecipe)
    }

    /**region Lingerings*/
    @Test
    fun addLingeringIntroOverlayWithAnimation_shouldMakeLingeringIntroAnimation_staticAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.FADE_IN)
        playerViewWrapper.addLingeringIntroOverlay(overlayEntity, 100L, true)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.introTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.introTransitionSpec.animationDuration, animationRecipe?.animationDuration)
        assertEquals(100L, animationRecipe?.animationPosition)
        assertEquals(true, animationRecipe?.isPlaying)
    }

    @Test
    fun updateLingeringIntroOverlayWithAnimation_shouldMakeLingeringIntroAnimation_staticAnimation() {
        viewIdentifierManager.idlingResource.dumpStateToLogs()
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())
        val overlayEntity = getSampleOverlayEntity(AnimationType.FADE_IN)
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.updateLingeringIntroOverlay(overlayEntity, 100L, true)

        }


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.introTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.introTransitionSpec.animationDuration, animationRecipe?.animationDuration)
        assertEquals(100L, animationRecipe?.animationPosition)
        assertEquals(true, animationRecipe?.isPlaying)
    }

    @Test
    fun addLingeringIntroOverlayWithAnimation_shouldMakeLingeringIntroAnimation_dynamicAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.SLIDE_FROM_LEFT)
        playerViewWrapper.addLingeringIntroOverlay(overlayEntity, 100L, true)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.introTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.introTransitionSpec.animationDuration, animationRecipe?.animationDuration)
        assertEquals(100L, animationRecipe?.animationPosition)
        assertEquals(true, animationRecipe?.isPlaying)
    }

    @Test
    fun addLingeringOutroOverlayWithAnimation_shouldMakeLingeringOutroAnimation_staticAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.UNSPECIFIED, AnimationType.FADE_OUT)
        playerViewWrapper.addLingeringOutroOverlay(overlayEntity, 100L, true)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.outroTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.outroTransitionSpec.animationDuration, animationRecipe?.animationDuration)
        assertEquals(100L, animationRecipe?.animationPosition)
        assertEquals(true, animationRecipe?.isPlaying)
    }

    @Test
    fun addLingeringOutroOverlayWithAnimation_shouldMakeLingeringOutroAnimation_dynamicAnimation() {
        val overlayEntity = getSampleOverlayEntity(AnimationType.UNSPECIFIED, AnimationType.SLIDE_TO_LEFT)
        playerViewWrapper.addLingeringOutroOverlay(overlayEntity, 100L, true)


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.outroTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.outroTransitionSpec.animationDuration, animationRecipe?.animationDuration)
        assertEquals(100L, animationRecipe?.animationPosition)
        assertEquals(true, animationRecipe?.isPlaying)
    }

    @Test
    fun updateLingeringOutroOverlayWithAnimation_shouldMakeLingeringIntroAnimation_staticAnimation() {
        viewIdentifierManager.idlingResource.dumpStateToLogs()
        playerViewWrapper.onNewOverlayWithNoAnimation(getSampleOverlayEntity())


        val overlayEntity = getSampleOverlayEntity(AnimationType.UNSPECIFIED, AnimationType.SLIDE_TO_LEFT)
        UiThreadStatement.runOnUiThread {
            playerViewWrapper.updateLingeringOutroOverlay(overlayEntity, 100L, true)
        }


        Espresso.onView(ViewMatchers.withClassName(TypeMatcher(ScaffoldView::class.java.canonicalName)))
            .check(
                ViewAssertions.matches(
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
                )
            )

        val animationRecipe = animationHelper.animationRecipe
        assertEquals(overlayEntity.outroTransitionSpec.animationType, animationRecipe?.animationType)
        assertEquals(overlayEntity.outroTransitionSpec.animationDuration, animationRecipe?.animationDuration)
        assertEquals(100L, animationRecipe?.animationPosition)
        assertEquals(true, animationRecipe?.isPlaying)
    }


    /**endregion */


    /**region Test data*/
    private fun getSampleOverlayEntity(): OverlayEntity {
        val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))

        val svgData = SvgData(null, null, sampleSvgString)

        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 0L)
        val outroTransitionSpec = TransitionSpec(2000L, AnimationType.NONE, 0L)

        return OverlayEntity(
            SAMPLE_ID,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }

    private fun getSampleOverlayEntity(introAnimationType: AnimationType): OverlayEntity {
        val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))

        val svgData = SvgData(null, null, sampleSvgString)

        val introTransitionSpec = TransitionSpec(1000L, introAnimationType, 600L)
        val outroTransitionSpec = TransitionSpec(2000L, AnimationType.NONE, 3000L)

        return OverlayEntity(
            SAMPLE_ID,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }

    private fun getSampleOverlayEntity(
        introAnimationType: AnimationType,
        outroAnimationType: AnimationType
    ): OverlayEntity {
        val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))

        val svgData = SvgData(null, null, sampleSvgString)

        val introTransitionSpec = TransitionSpec(1000L, introAnimationType, 300L)
        val outroTransitionSpec = TransitionSpec(2000L, outroAnimationType, 600L)

        return OverlayEntity(
            SAMPLE_ID,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }

    companion object {
        const val SAMPLE_ID = "id_1001"
    }

    /**endregion */
}