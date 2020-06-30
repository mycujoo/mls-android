package tv.mycujoo.mls.helper

import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.test.espresso.idling.CountingIdlingResource
import com.caverock.androidsvg.SVGImageView
import tv.mycujoo.mls.entity.actions.LayoutPosition
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView

class OverlayViewHelper {
    companion object {

        fun addView(
            host: OverlayHost,
            overlay: ViewGroup,
            position: LayoutPosition,
            idlingResource: CountingIdlingResource
        ) {

            when (position) {
                LayoutPosition.TOP_LEFT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    host.addView(overlay, layoutParams)
                }
                LayoutPosition.TOP_RIGHT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    host.addView(overlay, layoutParams)
                }
                LayoutPosition.BOTTOM_RIGHT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                    host.addView(overlay, layoutParams)
                }
                LayoutPosition.BOTTOM_LEFT -> {
                    val layoutParams = RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    host.addView(overlay, layoutParams)
                }
            }

            if (!idlingResource.isIdleNow) {
                idlingResource.decrement()
            }

        }

        fun removeInFuture(
            host: OverlayHost,
            overlayView: ViewGroup,
            dismissIn: Long,
            idlingResource: CountingIdlingResource
        ) {
            host.postDelayed({
                host.removeView(overlayView)

                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }, dismissIn)
        }


        fun addView(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            size: Pair<Float, Float>,
            idlingResource: CountingIdlingResource
        ) {
            host.post {
                val overlayLayoutParams: ConstraintLayout.LayoutParams


                val hostLayoutParams = host.layoutParams as ConstraintLayout.LayoutParams

                when {
                    size.first > 0F -> {
                        overlayLayoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )

//                        overlayLayoutParams.matchConstraintPercentWidth = size.first / 100
//                        overlayLayoutParams.matchConstraintPercentHeight = 1F
                    }
                    size.second > 0F -> {
                        overlayLayoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT

                            )
//                        overlayLayoutParams.matchConstraintPercentWidth = 1F
//                        overlayLayoutParams.matchConstraintPercentHeight = size.second / 100
                    }
                    else -> {
                        overlayLayoutParams = ConstraintLayout.LayoutParams(
                            ConstraintLayout.LayoutParams.WRAP_CONTENT,
                            ConstraintLayout.LayoutParams.WRAP_CONTENT
                        )
                    }
                }

                host.addView(proportionalImageView, overlayLayoutParams)
//                when {
//                    size.first > 0F -> {
//                        val set = ConstraintSet()
//                        set.clone(host)
//
//                        set.connect(overlay.id, ConstraintSet.TOP, host.id, ConstraintSet.TOP)
//                        set.connect(overlay.id, ConstraintSet.RIGHT, host.id, ConstraintSet.RIGHT)
//                        set.connect(overlay.id, ConstraintSet.BOTTOM, host.id, ConstraintSet.BOTTOM)
//                        set.connect(overlay.id, ConstraintSet.LEFT, host.id, ConstraintSet.LEFT)
//
//                        set.applyTo(host)
//                    }
//                    size.second > 0F -> {
//                        val set = ConstraintSet()
//                        set.clone(host)
//
//                        set.connect(overlay.id, ConstraintSet.TOP, host.id, ConstraintSet.TOP)
//                        set.connect(overlay.id, ConstraintSet.RIGHT, host.id, ConstraintSet.RIGHT)
//                        set.connect(overlay.id, ConstraintSet.BOTTOM, host.id, ConstraintSet.BOTTOM)
//                        set.connect(overlay.id, ConstraintSet.LEFT, host.id, ConstraintSet.LEFT)
//
//                        set.applyTo(host)
//                    }
//
//                }


                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }

        }

    }
}
