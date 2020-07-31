package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.FADE_IN
import tv.mycujoo.domain.entity.AnimationType.FADE_OUT
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

class AnimationFactory {

    companion object {

        /**region Lingering overlay animation*/
        fun createLingeringIntroAnimation(
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
                FADE_IN -> {
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

        fun createLingeringOutroAnimation(
            overlayHost: OverlayHost,
            scaffoldView: ScaffoldView,
            overlayEntity: OverlayEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ): ObjectAnimator? {


            var animation: ObjectAnimator? = null

            when (overlayEntity.outroTransitionSpec.animationType) {
                FADE_OUT -> {
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

        /**endregion */
    }
}
