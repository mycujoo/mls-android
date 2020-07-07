package tv.mycujoo.mls.helper

import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.AnimationType.*
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
                NONE,
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING,
                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
                    // should not happen
                    animation = null
                }
                FADE_IN -> {
                    animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 0F, 1F)
                    animation.duration = animationDuration
                }
                FADE_OUT -> {
                    animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 1F, 0F)
                    animation.duration = animationDuration
                }
            }
            return animation
        }

    }
}