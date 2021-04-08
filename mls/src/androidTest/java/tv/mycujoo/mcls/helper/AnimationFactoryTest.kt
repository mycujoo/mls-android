package tv.mycujoo.mcls.helper

import android.animation.ObjectAnimator
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import junit.framework.Assert.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.TestData.Companion.getSampleShowOverlayAction
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.fake.FakeOverlayHost
import tv.mycujoo.fake.FakeViewHandler
import tv.mycujoo.mcls.BlankActivity
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.widgets.ScaffoldView

/**
 * Tests the following property of Animation:
 * Animation property (X, Y or ALPHA)
 * Animation total duration
 * Animation current position (if applicable)
 * Animation isPlaying (if applicable)
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AnimationFactoryTest {


    /**region Subject under test*/
    private lateinit var animationFactory: AnimationFactory
    /**endregion */

    /**region Fields*/
    private lateinit var viewHandler: FakeViewHandler
    private var countingIdlingResource = CountingIdlingResource("ViewHandler")


    private lateinit var overlayHost: FakeOverlayHost
    private lateinit var scaffoldView: ScaffoldView

    /**endregion */


    @Before
    fun setUp() {
        animationFactory = AnimationFactory()

        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)

            overlayHost = FakeOverlayHost(activity)
            frameLayout.addView(overlayHost)

            scaffoldView = ScaffoldView(30F, -1F, activity)
            scaffoldView.id = View.generateViewId()
            scaffoldView.tag = "sample_tag"


            val job = SupervisorJob()

            val coroutineScope =
                CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
            viewHandler = FakeViewHandler(countingIdlingResource)
            viewHandler.setOverlayHost(overlayHost)

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

    /**region createAddViewStaticAnimation() tests*/
    @Test
    fun createAddViewStaticAnimation_ShouldCreateAnimationWithGivenDuration() {
        val objectAnimator =
            animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.FADE_IN, 123L)


        assertEquals(123L, objectAnimator!!.totalDuration)
    }

    @Test
    fun givenFade_IN_createAddViewStaticAnimationShouldCreateAnimationOnAlphaProperty() {
        val objectAnimator =
            animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.FADE_IN, 123L)


        assertEquals(View.ALPHA.name, objectAnimator!!.propertyName)
    }

    @Test
    fun givenInvalidStaticIntroAnimation_createAddViewStaticAnimationShouldReturnNull() {
        val objectAnimatorForAnimationTypeNONE =
            animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.NONE, 123L)
        val objectAnimatorForAnimationTypeFADEOUT =
            animationFactory.createAddViewStaticAnimation(
                scaffoldView,
                AnimationType.FADE_OUT,
                123L
            )


        assertNull(objectAnimatorForAnimationTypeNONE)
        assertNull(objectAnimatorForAnimationTypeFADEOUT)
    }
    /**endregion */

    /**region createAddViewDynamicAnimation() tests*/

    @Test
    fun createAddViewDynamicAnimation_ShouldCreateAnimationWithGivenDuration() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_FROM_TOP, 123L)


        val anim = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpec,
            viewHandler
        )


        assertEquals(123L, anim!!.totalDuration)
    }

    @Test
    fun givenSLIDE_FROM_TOP_createAddViewDynamicAnimationShouldCreateAnimationOnYProperty() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_FROM_TOP, 123L)


        val anim = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpec,
            viewHandler
        )


        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_BOTTOM_createAddViewDynamicAnimationShouldCreateAnimationOnYProperty() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_FROM_BOTTOM, 123L)


        val anim = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpec,
            viewHandler
        )


        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_LEFT_createAddViewDynamicAnimationShouldCreateAnimationOnXProperty() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_FROM_LEFT, 123L)


        val anim = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpec,
            viewHandler
        )


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_RIGHT_createAddViewDynamicAnimationShouldCreateAnimationOnXProperty() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_FROM_RIGHT, 123L)


        val anim = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpec,
            viewHandler
        )


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenInvalidIntroDynamicAnimation_createAddViewDynamicAnimationShouldReturnNull() {
        val transitionSpecForNone = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val objectAnimatorForAnimationTypeNONE = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForNone,
            viewHandler
        )

        val transitionSpecForFadeOut =
            TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_OUT, 123L)
        val objectAnimatorForAnimationTypeFADEOUT = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForFadeOut,
            viewHandler
        )

        val transitionSpecForFadeIn = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_IN, 123L)
        val objectAnimatorForAnimationTypeFADEIN = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForFadeIn,
            viewHandler
        )


        assertNull(objectAnimatorForAnimationTypeNONE)
        assertNull(objectAnimatorForAnimationTypeFADEOUT)
        assertNull(objectAnimatorForAnimationTypeFADEIN)
    }
    /**endregion */

    /**region createRemoveViewStaticAnimation() tests*/
    @Test
    fun createRemoveViewStaticAnimation_ShouldCreateAnimationWithGivenDuration() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_OUT, 123L)


        val anim = animationFactory.createRemoveViewStaticAnimation(
            overlayHost,
            getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )

        assertEquals(123L, anim.totalDuration)
    }

    @Test
    fun givenFADE_OUT_createRemoveViewStaticAnimationShouldCreateAnimationOnALPHAProperty() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_OUT, 123L)


        val anim = animationFactory.createRemoveViewStaticAnimation(
            overlayHost,
            getSampleShowOverlayAction(transitionSpec, ONE_SECOND_IN_MS),
            scaffoldView,
            viewHandler
        )

        assertEquals(View.ALPHA.name, anim.propertyName)
    }

    @Test
    fun createRemoveViewStaticAnimation_ShouldRemoveAnimationAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewStaticAnimation(
                overlayHost,
                action,
                scaffoldView,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(action.id, viewHandler.lastRemovedAnimationId)
    }

    @Test
    fun createRemoveViewStaticAnimation_ShouldRemoveViewAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewStaticAnimation(
                overlayHost,
                action,
                scaffoldView,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(scaffoldView, overlayHost.lastRemovedView)
    }
    /**endregion */

    /**region createRemoveViewDynamicAnimation() tests*/
    @Test
    fun createRemoveViewDynamicAnimation_ShouldCreateAnimationWithGivenDuration() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_TOP, 123L)


        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)
        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            action.id,
            action.outroTransitionSpec!!,
            scaffoldView,
            viewHandler
        )

        assertEquals(123L, anim!!.totalDuration)
    }

    @Test
    fun givenSLIDE_TO_TOP_createRemoveViewDynamicAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_TOP, 123L)

        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)
        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            action.id,
            action.outroTransitionSpec!!,
            scaffoldView,
            viewHandler
        )

        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_BOTTOM_createRemoveViewDynamicAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec =
            TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_BOTTOM, 123L)

        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            action.id,
            action.outroTransitionSpec!!,
            scaffoldView,
            viewHandler
        )

        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_LEFT_createRemoveViewDynamicAnimationShouldCreateAnimationOnXProperty() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec =
            TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_LEFT, 123L)

        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            action.id,
            action.outroTransitionSpec!!,
            scaffoldView,
            viewHandler
        )


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_RIGHT_createRemoveViewDynamicAnimationShouldCreateAnimationOnXProperty() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec =
            TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_RIGHT, 123L)

        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            action.id,
            action.outroTransitionSpec!!,
            scaffoldView,
            viewHandler
        )


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenInvalidOutroDynamicAnimation_createRemoveViewDynamicAnimationShouldReturnNull() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpecForNone = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)

        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpecForNone)

        val animForNone = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            action.id,
            action.outroTransitionSpec!!,
            scaffoldView,
            viewHandler
        )

        val transitionSpecForFadeOut =
            TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_OUT, 123L)
        val animForFADEOUT = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForFadeOut,
            viewHandler
        )

        val transitionSpecForFadeIn = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_IN, 123L)
        val animForFADEIN = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForFadeIn,
            viewHandler
        )


        assertNull(animForNone)
        assertNull(animForFADEOUT)
        assertNull(animForFADEIN)
    }

    @Test
    fun createRemoveViewDynamicAnimation_ShouldRemoveAnimationAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_TOP, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator?
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewDynamicAnimation(
                overlayHost = overlayHost,
                actionId = action.id,
                outroTransitionSpec = action.outroTransitionSpec!!,
                view = scaffoldView,
                viewHandler = viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(action.id, viewHandler.lastRemovedAnimationId)
    }

    @Test
    fun createRemoveViewDynamicAnimation_ShouldRemoveViewAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.SLIDE_TO_TOP, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator?
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewDynamicAnimation(
                overlayHost,
                action.id,
                action.outroTransitionSpec!!,
                scaffoldView,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(scaffoldView, overlayHost.lastRemovedView)
    }


    /**endregion */

    /**region createLingeringIntroViewAnimation() test*/
    @Test
    fun createLingeringIntroViewAnimation_ShouldCreateAnimationWithGivenDuration() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_IN, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, ONE_SECOND_IN_MS)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(123L, anim!!.totalDuration)
    }

    @Test
    fun createLingeringIntroViewAnimation_ShouldCreateAnimationWithGivenAnimationPosition() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_IN, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, ONE_SECOND_IN_MS)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(57L, anim!!.currentPlayTime)
    }

    @Test
    fun createLingeringIntroViewAnimation_ShouldCreateAnimationWithAnimationIsPlayingState() {
        val transitionSpec = TransitionSpec(ONE_SECOND_IN_MS, AnimationType.FADE_IN, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, ONE_SECOND_IN_MS)


        var animPlaying: ObjectAnimator? = null
        var animPaused: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            animPlaying = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
            animPaused = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                false,
                viewHandler
            )
        }


        assertFalse(animPlaying!!.isPaused)
        assertTrue(animPaused!!.isPaused)
    }

    @Test
    fun givenFADE_IN_createLingeringIntroViewAnimationShouldCreateAnimationOnALPHAProperty() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.ALPHA.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_TOP_createLingeringIntroViewAnimationShouldCreateAnimationOnYProperty() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_TOP, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_BOTTOM_createLingeringIntroViewAnimationShouldCreateAnimationOnYProperty() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_BOTTOM, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_LEFT_createLingeringIntroViewAnimationShouldCreateAnimationOnYProperty() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_LEFT, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_FROM_RIGHT_createLingeringIntroViewAnimationShouldCreateAnimationOnYProperty() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_RIGHT, 123L)
        val action = getSampleShowOverlayAction(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenInvalidIntroAnimation_createLingeringIntroViewAnimationShouldReturnNull() {
        val transitionSpecForNone = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val actionForNone = getSampleShowOverlayAction(transitionSpecForNone, 1000L)

        val transitionSpecForFadeOut = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val actionForFadeOut = getSampleShowOverlayAction(transitionSpecForFadeOut, 1000L)

        val transitionSpecForSlideToLeft = TransitionSpec(1000L, AnimationType.SLIDE_TO_LEFT, 123L)
        val actionForSlideToLeft =
            getSampleShowOverlayAction(transitionSpecForSlideToLeft, 1000L)


        var animForAnimationNone: ObjectAnimator? = null
        var animForAnimationFadeOut: ObjectAnimator? = null
        var animForAnimationSlideToLeft: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            animForAnimationNone = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                actionForNone,
                57L,
                true,
                viewHandler
            )

            animForAnimationFadeOut = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                actionForFadeOut,
                57L,
                true,
                viewHandler
            )
            animForAnimationSlideToLeft = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                actionForSlideToLeft,
                57L,
                true,
                viewHandler
            )
        }


        assertNull(animForAnimationNone)
        assertNull(animForAnimationFadeOut)
        assertNull(animForAnimationSlideToLeft)
    }

    @Test
    fun createLingeringIntroAnimation_ShouldRemoveAnimationAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, 1000L)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(action.id, viewHandler.lastRemovedAnimationId)
    }
    /**endregion*/

    /**region createLingeringOutroAnimation() tests*/
    @Test
    fun createLingeringOutroAnimation_ShouldCreateAnimationWithGivenDuration() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(123L, anim!!.totalDuration)
    }

    @Test
    fun createLingeringOutroAnimation_ShouldCreateAnimationWithGivenAnimationPosition() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(57L, anim!!.currentPlayTime)
    }

    @Test
    fun createLingeringOutroAnimation_ShouldCreateAnimationWithGivenAnimationIsPlayingState() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var animPlaying: ObjectAnimator? = null
        var animPause: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            animPlaying = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
            animPause = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                false,
                viewHandler
            )
        }


        assertFalse(animPlaying!!.isPaused)
        assertTrue(animPause!!.isPaused)
    }

    @Test
    fun givenFADE_OUT_createLingeringOutroAnimationShouldCreateAnimationOnALPHAProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.ALPHA.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_TOP_createLingeringOutroAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_TOP, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_BOTTOM_createLingeringOutroAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_BOTTOM, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_RIGHT_createLingeringOutroAnimationShouldCreateAnimationOnXProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_RIGHT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_LEFT_createLingeringOutroAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_LEFT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun createLingeringOutroAnimation_ShouldRemoveAnimationAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(action.id, viewHandler.lastRemovedAnimationId)
    }

    @Test
    fun createLingeringOutroAnimation_ShouldRemoveViewAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val action = getSampleShowOverlayAction(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                action,
                57L,
                true,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(scaffoldView, overlayHost.lastRemovedView)
    }


    /**endregion */

    /**region Helper*/
    private fun simulateAnimationEnd(objectAnimator: ObjectAnimator?) {
        val animatorListener = objectAnimator!!.listeners[0]
        animatorListener.onAnimationEnd(objectAnimator)
    }

    /**endregion */
}