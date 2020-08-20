package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

open class AnimationFactory {
    open fun createAddViewStaticAnimation(
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
        viewHandler: IViewHandler
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
            AnimationType.SLIDE_FROM_TOP -> {
                scaffoldView.y =
                    -scaffoldView.height.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.Y,
                    scaffoldView.y,
                    y
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
            AnimationType.SLIDE_FROM_BOTTOM -> {
                scaffoldView.y =
                    overlayHost.height.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.Y,
                    scaffoldView.y,
                    y
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
            viewHandler.addAnimation(
                scaffoldView.tag as String,
                animation
            )
        }

        return animation

    }

    open fun createRemoveViewStaticAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        overlayView: ScaffoldView,
        viewHandler: IViewHandler
    ): ObjectAnimator {
        val animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 1F, 0F)
        animation.duration = overlayEntity.outroTransitionSpec.animationDuration

        animation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                viewHandler.detachOverlayView(overlayView)
                viewHandler.removeAnimation(overlayEntity.id)
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

        })

        viewHandler.addAnimation(overlayEntity.id, animation)

        return animation

    }

    open fun createRemoveViewDynamicAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        view: ScaffoldView,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        var animation: ObjectAnimator? = null

        when (overlayEntity.outroTransitionSpec.animationType) {
            AnimationType.SLIDE_TO_LEFT -> {
                animation = ObjectAnimator.ofFloat(
                    view,
                    View.X,
                    view.x,
                    -view.width.toFloat()
                )
            }
            AnimationType.SLIDE_TO_TOP -> {
                animation = ObjectAnimator.ofFloat(
                    view,
                    View.Y,
                    view.y,
                    -view.height.toFloat()
                )
            }
            AnimationType.SLIDE_TO_RIGHT -> {
                animation = ObjectAnimator.ofFloat(
                    view,
                    View.X,
                    view.x,
                    overlayHost.width.toFloat()
                )
            }
            AnimationType.SLIDE_TO_BOTTOM -> {
                animation = ObjectAnimator.ofFloat(
                    view,
                    View.Y,
                    view.y,
                    overlayHost.height.toFloat()
                )
            }
            else -> {
                // should not happen
            }
        }

        if (animation != null) {
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    viewHandler.detachOverlayView(view)
                    viewHandler.removeAnimation(overlayEntity.id)
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })

            animation.duration = overlayEntity.outroTransitionSpec.animationDuration
            viewHandler.addAnimation(overlayEntity.id, animation)

        }
        return animation

    }

    open fun createLingeringIntroViewAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        val x = scaffoldView.x
        val y = scaffoldView.y

        var animation: ObjectAnimator? = null


        when (overlayEntity.introTransitionSpec.animationType) {
            AnimationType.FADE_IN -> {
                scaffoldView.x =
                    -scaffoldView.width.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.ALPHA,
                    0F,
                    1F
                )
            }
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
            AnimationType.SLIDE_FROM_TOP -> {
                scaffoldView.y =
                    -scaffoldView.height.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.Y,
                    scaffoldView.y,
                    y
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
            AnimationType.SLIDE_FROM_BOTTOM -> {
                scaffoldView.y =
                    overlayHost.height.toFloat()
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.Y,
                    scaffoldView.y,
                    y
                )
            }
            else -> {
                // should not happen
            }
        }

        if (animation != null) {
            animation.duration =
                overlayEntity.introTransitionSpec.animationDuration
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    viewHandler.removeAnimation(overlayEntity.id)
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    scaffoldView.visibility = View.VISIBLE

                }

            })

            viewHandler.addAnimation(
                overlayEntity.id,
                animation
            )
            animation.start()
            animation.currentPlayTime = animationPosition
            if (isPlaying) {
                animation.resume()
            } else {
                animation.pause()
            }
        }

        return animation

    }

    open fun createLingeringOutroAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewHandler: IViewHandler
    ): ObjectAnimator? {
        var animation: ObjectAnimator? = null

        when (overlayEntity.outroTransitionSpec.animationType) {
            AnimationType.FADE_OUT -> {
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.ALPHA,
                    1F,
                    0F
                )
            }
            AnimationType.SLIDE_TO_LEFT -> {

                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.X,
                    scaffoldView.x,
                    -scaffoldView.width.toFloat()
                )

            }
            AnimationType.SLIDE_TO_TOP -> {
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.Y,
                    scaffoldView.y,
                    -overlayHost.height.toFloat()
                )
            }
            AnimationType.SLIDE_TO_RIGHT -> {
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.X,
                    scaffoldView.x,
                    overlayHost.width.toFloat()
                )

            }
            AnimationType.SLIDE_TO_BOTTOM -> {
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.Y,
                    scaffoldView.y,
                    overlayHost.height.toFloat()
                )
            }
            else -> {
                // should not happen
            }
        }

        if (animation != null) {
            animation.duration =
                overlayEntity.outroTransitionSpec.animationDuration
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    viewHandler.removeAnimation(overlayEntity.id)
                    viewHandler.detachOverlayView(
                        scaffoldView
                    )
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    scaffoldView.visibility = View.VISIBLE
                }

            })

            viewHandler.addAnimation(
                overlayEntity.id,
                animation
            )
            animation.start()
            animation.currentPlayTime = animationPosition
            if (isPlaying) {
                animation.resume()
            } else {
                animation.pause()
            }
        }


        return animation
    }
}