package tv.mycujoo.fake

import android.animation.ObjectAnimator
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

class FakeAnimationFactory : AnimationFactory() {

    var animationRecipe: AnimationRecipe? = null


    override fun createStaticAnimation(
        scaffoldView: ScaffoldView,
        animationType: AnimationType,
        animationDuration: Long
    ): ObjectAnimator? {

        animationRecipe = AnimationRecipe(scaffoldView, animationType, animationDuration)

        return super.createStaticAnimation(scaffoldView, animationType, animationDuration)
    }

    override fun createAddViewDynamicAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        introTransitionSpec: TransitionSpec,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator? {

        animationRecipe =
            AnimationRecipe(scaffoldView, introTransitionSpec.animationType, introTransitionSpec.animationDuration)

        return super.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            introTransitionSpec,
            viewIdentifierManager
        )
    }

    override fun createRemoveViewStaticAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        overlayView: ScaffoldView,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator {

        val outroTransitionSpec = overlayEntity.outroTransitionSpec
        animationRecipe =
            AnimationRecipe(overlayView, outroTransitionSpec.animationType, outroTransitionSpec.animationDuration)

        return super.createRemoveViewStaticAnimation(overlayHost, overlayEntity, overlayView, viewIdentifierManager)
    }

    override fun createRemoveViewDynamicAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        overlayView: ScaffoldView,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator? {
        val outroTransitionSpec = overlayEntity.outroTransitionSpec
        animationRecipe =
            AnimationRecipe(overlayView, outroTransitionSpec.animationType, outroTransitionSpec.animationDuration)

        return super.createRemoveViewDynamicAnimation(overlayHost, overlayEntity, overlayView, viewIdentifierManager)
    }

    override fun createLingeringIntroViewAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator? {
        val introTransitionSpec = overlayEntity.introTransitionSpec

        animationRecipe =
            AnimationRecipe(
                scaffoldView,
                introTransitionSpec.animationType,
                introTransitionSpec.animationDuration,
                animationPosition,
                isPlaying
            )

        return super.createLingeringIntroViewAnimation(
            overlayHost,
            scaffoldView,
            overlayEntity,
            animationPosition,
            isPlaying,
            viewIdentifierManager
        )
    }

    override fun createLingeringOutroAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator? {
        val outroTransitionSpec = overlayEntity.outroTransitionSpec

        animationRecipe =
            AnimationRecipe(
                scaffoldView,
                outroTransitionSpec.animationType,
                outroTransitionSpec.animationDuration,
                animationPosition,
                isPlaying
            )

        return super.createLingeringOutroAnimation(
            overlayHost,
            scaffoldView,
            overlayEntity,
            animationPosition,
            isPlaying,
            viewIdentifierManager
        )
    }

    data class AnimationRecipe(
        val scaffoldView: ScaffoldView,
        val animationType: AnimationType,
        val animationDuration: Long,
        val animationPosition: Long = -1L,
        val isPlaying: Boolean = false
    )

}