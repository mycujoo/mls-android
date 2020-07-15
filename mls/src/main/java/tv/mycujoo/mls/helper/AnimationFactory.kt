package tv.mycujoo.mls.helper

import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.FADE_IN
import tv.mycujoo.domain.entity.AnimationType.FADE_OUT
import tv.mycujoo.mls.widgets.ProportionalImageView

class AnimationFactory {

    companion object {
        fun createStaticAnimation(
            overlayView: ProportionalImageView,
            animationType: AnimationType,
            animationDuration: Long
        ): ObjectAnimator? {

            val animation: ObjectAnimator?
            when (animationType) {
                FADE_IN -> {
                    animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 0F, 1F)
                    animation.duration = animationDuration
                }
                FADE_OUT -> {
                    animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 1F, 0F)
                    animation.duration = animationDuration
                }
                else -> {
                    // should not happen
                    animation = null
                }

            }
            return animation
        }

    }
}