package tv.mycujoo.fake

import android.animation.ObjectAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mcls.helper.AnimationFactory
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.widgets.ScaffoldView

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
        overlayHost: ConstraintLayout,
        scaffoldView: ScaffoldView,
        introTransitionSpec: TransitionSpec,
        viewHandler: IViewHandler
    ): ObjectAnimator? {

        animationRecipe =
            AnimationRecipe(
                scaffoldView,
                introTransitionSpec.animationType,
                introTransitionSpec.animationDuration
            )

        return super.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            introTransitionSpec,
            viewHandler
        )
    }

    override fun createRemoveViewStaticAnimation(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        overlayView: ScaffoldView,
        viewHandler: IViewHandler
    ): ObjectAnimator {

        val outroTransitionSpec = showOverlayAction.outroTransitionSpec
        animationRecipe =
            AnimationRecipe(
                overlayView,
                outroTransitionSpec!!.animationType,
                outroTransitionSpec.animationDuration
            )

        return super.createRemoveViewStaticAnimation(
            overlayHost,
            showOverlayAction,
            overlayView,
            viewHandler
        )
    }

    override fun createRemoveViewDynamicAnimation(
        overlayHost: ConstraintLayout,
        actionId: String,
        outroTransitionSpec: TransitionSpec,
        overlayView: ScaffoldView,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        animationRecipe =
            AnimationRecipe(
                overlayView,
                outroTransitionSpec.animationType,
                outroTransitionSpec.animationDuration
            )

        return super.createRemoveViewDynamicAnimation(
            overlayHost, actionId, outroTransitionSpec,
            overlayView, viewHandler
        )
    }

    override fun createLingeringIntroViewAnimation(
        overlayHost: ConstraintLayout,
        scaffoldView: ScaffoldView,
        overlayEntity: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        val introTransitionSpec = overlayEntity.introTransitionSpec

        animationRecipe =
            AnimationRecipe(
                scaffoldView,
                introTransitionSpec!!.animationType,
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
        overlayHost: ConstraintLayout,
        scaffoldView: ScaffoldView,
        overlayEntity: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        val outroTransitionSpec = overlayEntity.outroTransitionSpec

        animationRecipe =
            AnimationRecipe(
                scaffoldView,
                outroTransitionSpec!!.animationType,
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