package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
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

    open fun createRemoveViewStaticAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        overlayView: ScaffoldView,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator {
        val animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 1F, 0F)
        animation.duration = overlayEntity.outroTransitionSpec.animationDuration

        animation.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                overlayHost.removeView(overlayView)
                viewIdentifierManager.detachOverlayView(overlayView)
                viewIdentifierManager.removeAnimation(overlayEntity.id)
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

        })

        viewIdentifierManager.addAnimation(overlayEntity.id, animation)

        return animation

    }

    open fun createRemoveViewDynamicAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        view: ScaffoldView,
        viewIdentifierManager: ViewIdentifierManager
    ): ObjectAnimator? {
        var animation: ObjectAnimator? = null

        if (overlayEntity.outroTransitionSpec.animationType == AnimationType.SLIDE_TO_LEFT) {
            animation = ObjectAnimator.ofFloat(
                view,
                View.X,
                view.x,
                -view.width.toFloat()
            )
        } else if (overlayEntity.outroTransitionSpec.animationType == AnimationType.SLIDE_TO_RIGHT) {
            animation = ObjectAnimator.ofFloat(
                view,
                View.X,
                view.x,
                overlayHost.width.toFloat()
            )

        }

        if (animation != null) {
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    overlayHost.removeView(view)
                    viewIdentifierManager.detachOverlayView(view)
                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                }

            })

            animation.duration = overlayEntity.outroTransitionSpec.animationDuration
            viewIdentifierManager.addAnimation(overlayEntity.id, animation)

        }
        return animation

    }

    open fun createLingeringIntroViewAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean,
        viewIdentifierManager: ViewIdentifierManager
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
            animation.duration =
                overlayEntity.introTransitionSpec.animationDuration
            animation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    scaffoldView.visibility = View.VISIBLE

                }

            })

            viewIdentifierManager.addAnimation(
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
        viewIdentifierManager: ViewIdentifierManager
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
            AnimationType.SLIDE_TO_RIGHT -> {
                animation = ObjectAnimator.ofFloat(
                    scaffoldView,
                    View.X,
                    scaffoldView.x,
                    overlayHost.width.toFloat()
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
                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                    viewIdentifierManager.detachOverlayView(
                        scaffoldView
                    )
                    overlayHost.removeView(scaffoldView)
                }

                override fun onAnimationCancel(animation: Animator?) {

                }

                override fun onAnimationStart(animation: Animator?) {
                    scaffoldView.visibility = View.VISIBLE
                }

            })

            viewIdentifierManager.addAnimation(
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