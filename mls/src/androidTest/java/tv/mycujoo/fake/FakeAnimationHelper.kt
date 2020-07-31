package tv.mycujoo.fake

import android.animation.ObjectAnimator
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.helper.AnimationHelper
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

class FakeAnimationHelper : AnimationHelper() {

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

        animationRecipe = AnimationRecipe(scaffoldView, introTransitionSpec.animationType, introTransitionSpec.animationDuration)

        return super.createAddViewDynamicAnimation(
            overlayHost,
            scaffoldView,
            introTransitionSpec,
            viewIdentifierManager
        )
    }

    data class AnimationRecipe(
        val scaffoldView: ScaffoldView,
        val animationType: AnimationType,
        val animationDuration: Long
    )

}