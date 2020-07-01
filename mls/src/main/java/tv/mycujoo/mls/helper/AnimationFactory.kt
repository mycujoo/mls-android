package tv.mycujoo.mls.helper

import android.animation.ObjectAnimator
import android.view.View
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.mls.widgets.ProportionalImageView

class AnimationFactory {

    companion object {
        fun create(
            overlayView: ProportionalImageView,
            animationType: AnimationType,
            animationDuration: Long
        ): ObjectAnimator {
            val animation = ObjectAnimator.ofFloat(overlayView, View.ALPHA, 0F, 1F)
            animation.duration = animationDuration
            return animation
        }

    }
}