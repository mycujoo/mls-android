package tv.mycujoo.fake

import android.animation.ObjectAnimator
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

class FakeAnimationFactory : AnimationFactory() {

    var animationRecipe: AnimationRecipe? = null


    override fun createAddViewStaticAnimation(
        scaffoldView: ScaffoldView,
        animationType: AnimationType,
        animationDuration: Long
    ): ObjectAnimator? {

        animationRecipe = AnimationRecipe(scaffoldView, animationType, animationDuration)

        return super.createAddViewStaticAnimation(scaffoldView, animationType, animationDuration)
    }

    override fun createAddViewDynamicAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        introTransitionSpec: TransitionSpec,
        viewHandler: IViewHandler
    ): ObjectAnimator? {

        animationRecipe =
            AnimationRecipe(scaffoldView, introTransitionSpec.animationType, introTransitionSpec.animationDuration)

        return super.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            introTransitionSpec,
            viewHandler
        )
    }

    override fun createRemoveViewStaticAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        overlayView: ScaffoldView,
        viewHandler: IViewHandler
    ): ObjectAnimator {

        val outroTransitionSpec = overlayEntity.outroTransitionSpec
        animationRecipe =
            AnimationRecipe(overlayView, outroTransitionSpec.animationType, outroTransitionSpec.animationDuration)

        return super.createRemoveViewStaticAnimation(overlayHost, overlayEntity, overlayView, viewHandler)
    }

    override fun createRemoveViewDynamicAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        overlayView: ScaffoldView,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        val outroTransitionSpec = overlayEntity.outroTransitionSpec
        animationRecipe =
            AnimationRecipe(overlayView, outroTransitionSpec.animationType, outroTransitionSpec.animationDuration)

        return super.createRemoveViewDynamicAnimation(overlayHost, overlayEntity, overlayView, viewHandler)
    }

    override fun createLingeringIntroViewAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewHandler: IViewHandler
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
            viewHandler
        )
    }

    override fun createLingeringOutroAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewHandler: IViewHandler
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
            viewHandler
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