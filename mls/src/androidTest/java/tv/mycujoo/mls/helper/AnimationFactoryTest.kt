package tv.mycujoo.mls.helper

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
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.domain.entity.ViewSpec
import tv.mycujoo.fake.FakeOverlayHost
import tv.mycujoo.fake.FakeViewHandler
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.ScaffoldView

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

            val coroutineScope = CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
            viewHandler = FakeViewHandler(coroutineScope, countingIdlingResource)
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
        val objectAnimator = animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.FADE_IN, 123L)


        assertEquals(123L, objectAnimator!!.totalDuration)
    }

    @Test
    fun givenFade_IN_createAddViewStaticAnimationShouldCreateAnimationOnAlphaProperty() {
        val objectAnimator = animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.FADE_IN, 123L)


        assertEquals(View.ALPHA.name, objectAnimator!!.propertyName)
    }

    @Test
    fun givenInvalidStaticIntroAnimation_createAddViewStaticAnimationShouldReturnNull() {
        val objectAnimatorForAnimationTypeNONE =
            animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.NONE, 123L)
        val objectAnimatorForAnimationTypeFADEOUT =
            animationFactory.createAddViewStaticAnimation(scaffoldView, AnimationType.FADE_OUT, 123L)


        assertNull(objectAnimatorForAnimationTypeNONE)
        assertNull(objectAnimatorForAnimationTypeFADEOUT)
    }
    /**endregion */

    /**region createAddViewDynamicAnimation() tests*/

    @Test
    fun createAddViewDynamicAnimation_ShouldCreateAnimationWithGivenDuration() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_TOP, 123L)


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
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_TOP, 123L)


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
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_BOTTOM, 123L)


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
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_LEFT, 123L)


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
        val transitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_FROM_RIGHT, 123L)


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
        val transitionSpecForNone = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val objectAnimatorForAnimationTypeNONE = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForNone,
            viewHandler
        )

        val transitionSpecForFadeOut = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val objectAnimatorForAnimationTypeFADEOUT = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForFadeOut,
            viewHandler
        )

        val transitionSpecForFadeIn = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
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
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)


        val anim = animationFactory.createRemoveViewStaticAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )

        assertEquals(123L, anim.totalDuration)
    }

    @Test
    fun givenFADE_OUT_createRemoveViewStaticAnimationShouldCreateAnimationOnALPHAProperty() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)


        val anim = animationFactory.createRemoveViewStaticAnimation(
            overlayHost,
            getSampleOverlayEntity(transitionSpec, 1000L),
            scaffoldView,
            viewHandler
        )

        assertEquals(View.ALPHA.name, anim.propertyName)
    }

    @Test
    fun createRemoveViewStaticAnimation_ShouldRemoveAnimationAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewStaticAnimation(
                overlayHost,
                overlayEntity,
                scaffoldView,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(overlayEntity.id, viewHandler.lastRemovedAnimationId)
    }

    @Test
    fun createRemoveViewStaticAnimation_ShouldRemoveViewAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewStaticAnimation(
                overlayHost,
                overlayEntity,
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
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_TOP, 123L)


        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )

        assertEquals(123L, anim!!.totalDuration)
    }

    @Test
    fun givenSLIDE_TO_TOP_createRemoveViewDynamicAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_TOP, 123L)


        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )

        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_BOTTOM_createRemoveViewDynamicAnimationShouldCreateAnimationOnYProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_BOTTOM, 123L)


        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )

        assertEquals(View.Y.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_LEFT_createRemoveViewDynamicAnimationShouldCreateAnimationOnXProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_LEFT, 123L)


        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenSLIDE_TO_RIGHT_createRemoveViewDynamicAnimationShouldCreateAnimationOnXProperty() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_RIGHT, 123L)


        val anim = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec),
            scaffoldView,
            viewHandler
        )


        assertEquals(View.X.name, anim!!.propertyName)
    }

    @Test
    fun givenInvalidOutroDynamicAnimation_createRemoveViewDynamicAnimationShouldReturnNull() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpecForNone = TransitionSpec(1000L, AnimationType.NONE, 123L)


        val animForNone = animationFactory.createRemoveViewDynamicAnimation(
            overlayHost,
            getSampleOverlayEntity(introTransitionSpec, outroTransitionSpecForNone),
            scaffoldView,
            viewHandler
        )

        val transitionSpecForFadeOut = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val animForFADEOUT = animationFactory.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            transitionSpecForFadeOut,
            viewHandler
        )

        val transitionSpecForFadeIn = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
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
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_TOP, 123L)
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewDynamicAnimation(
                overlayHost,
                overlayEntity,
                scaffoldView,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(overlayEntity.id, viewHandler.lastRemovedAnimationId)
    }

    @Test
    fun createRemoveViewDynamicAnimation_ShouldRemoveViewAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.SLIDE_TO_TOP, 123L)
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createRemoveViewDynamicAnimation(
                overlayHost,
                overlayEntity,
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
        val transitionSpec = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(123L, anim!!.totalDuration)
    }

    @Test
    fun createLingeringIntroViewAnimation_ShouldCreateAnimationWithGivenAnimationPosition() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
                57L,
                true,
                viewHandler
            )
        }


        assertEquals(57L, anim!!.currentPlayTime)
    }

    @Test
    fun createLingeringIntroViewAnimation_ShouldCreateAnimationWithAnimationIsPlayingState() {
        val transitionSpec = TransitionSpec(1000L, AnimationType.FADE_IN, 123L)
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var animPlaying: ObjectAnimator? = null
        var animPaused: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            animPlaying = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
                57L,
                true,
                viewHandler
            )
            animPaused = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(transitionSpec, 1000L)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntityForNone = getSampleOverlayEntity(transitionSpecForNone, 1000L)

        val transitionSpecForFadeOut = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val overlayEntityForFadeOut = getSampleOverlayEntity(transitionSpecForFadeOut, 1000L)

        val transitionSpecForSlideToLeft = TransitionSpec(1000L, AnimationType.SLIDE_TO_LEFT, 123L)
        val overlayEntityForSlideToLeft = getSampleOverlayEntity(transitionSpecForSlideToLeft, 1000L)


        var animForAnimationNone: ObjectAnimator? = null
        var animForAnimationFadeOut: ObjectAnimator? = null
        var animForAnimationSlideToLeft: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            animForAnimationNone = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntityForNone,
                57L,
                true,
                viewHandler
            )

            animForAnimationFadeOut = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntityForFadeOut,
                57L,
                true,
                viewHandler
            )
            animForAnimationSlideToLeft = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntityForSlideToLeft,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, 1000L)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringIntroViewAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
                57L,
                true,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(overlayEntity.id, viewHandler.lastRemovedAnimationId)
    }
    /**endregion*/

    /**region createLingeringOutroAnimation() tests*/
    @Test
    fun createLingeringOutroAnimation_ShouldCreateAnimationWithGivenDuration() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var animPlaying: ObjectAnimator? = null
        var animPause: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            animPlaying = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
                57L,
                true,
                viewHandler
            )
            animPause = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)


        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
                57L,
                true,
                viewHandler
            )
            simulateAnimationEnd(anim)
        }


        assertEquals(overlayEntity.id, viewHandler.lastRemovedAnimationId)
    }

    @Test
    fun createLingeringOutroAnimation_ShouldRemoveViewAfterAnimationEnds() {
        val introTransitionSpec = TransitionSpec(1000L, AnimationType.NONE, 123L)
        val outroTransitionSpec = TransitionSpec(1000L, AnimationType.FADE_OUT, 123L)
        val overlayEntity = getSampleOverlayEntity(introTransitionSpec, outroTransitionSpec)

        var anim: ObjectAnimator? = null
        UiThreadStatement.runOnUiThread {
            anim = animationFactory.createLingeringOutroAnimation(
                overlayHost,
                scaffoldView,
                overlayEntity,
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


    fun getSampleOverlayEntity(
        introTransitionSpec: TransitionSpec,
        outroOffset: Long
    ): OverlayEntity {
        val viewSpec = ViewSpec(null, null)

        val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)

        return OverlayEntity(
            "id_1001",
            null,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }

    fun getSampleOverlayEntity(
        introTransitionSpec: TransitionSpec,
        outroTransitionSpec: TransitionSpec

    ): OverlayEntity {
        val viewSpec = ViewSpec(null, null)

        return OverlayEntity(
            "id_1001",
            null,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            emptyList()
        )
    }


}