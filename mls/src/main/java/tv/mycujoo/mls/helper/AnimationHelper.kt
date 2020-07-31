package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

open class AnimationHelper {
    open fun createStaticAnimation(
        scaffoldView: ScaffoldView,
        animationType: AnimationType,
        animationDuration: Long
    ): ObjectAnimator? {
        val animation: ObjectAnimator?
        when (animationType) {
            AnimationType.FADE_IN -> {
                animation = ObjectAnimator.ofFloat(scaffoldView, View.ALPHA, 0F, 1F)
                animation.duration = animationDuration
            }
            AnimationType.FADE_OUT -> {
                animation = ObjectAnimator.ofFloat(scaffoldView, View.ALPHA, 1F, 0F)
                animation.duration = animationDuration
            }
            else -> {
                // should not happen
                animation = null
            }

        }
        return animation
    }

    open fun createAddViewDynamicAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        introTransitionSpec: TransitionSpec,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator? {
        val x = scaffoldView.x
        val y = scaffoldView.y

        var animation: ObjectAnimator? = null


        when (introTransitionSpec.animationType) {
            AnimationType.SLIDE_FROM_LEFT -> {
                scaffoldView.x =
                    -scaffoldView.width.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.X,
                    scaffoldView.x,
                    x
                )

            }
            AnimationType.SLIDE_FROM_RIGHT -> {
                scaffoldView.x = overlayHost.width.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.X,
                    scaffoldView.x,
                    x
                )
            }
            else -> {
                // should not happen
            }
        }

        if (animation != null) {
            animation.duration = introTransitionSpec.animationDuration
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    scaffoldView.visibility = View.VISIBLE
                }

            })
            viewIdentifierManager.addAnimation(
                scaffoldView.tag as String,
                animation
            )
        }

        return animation

    }
}