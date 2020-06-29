package tv.mycujoo.mls.helper

import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.idling.CountingIdlingResource
import com.caverock.androidsvg.SVGImageView
import tv.mycujoo.mls.entity.actions.LayoutPosition
import tv.mycujoo.mls.widgets.OverlayHost

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
            overlay: SVGImageView,
            size: Pair<Float, Float>,
            idlingResource: CountingIdlingResource
        ) {
            host.post {
                val overlayLayoutParams = ConstraintLayout.LayoutParams(
                    0,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                val hostLayoutParams = host.layoutParams as ConstraintLayout.LayoutParams

                if (size.first > 0){
                    overlayLayoutParams.matchConstraintPercentWidth = size.first / 100
                }

                host.addView(overlay, overlayLayoutParams)

                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }

        }

    }
}
