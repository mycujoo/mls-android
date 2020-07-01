package tv.mycujoo.mls.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.domain.entity.PositionGuide
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
            positionGuide: PositionGuide,
            idlingResource: CountingIdlingResource
        ) {
            host.post {


                val constraintSet = ConstraintSet()
                constraintSet.clone(host)
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )


                positionGuide.leading?.let {
                    if (it < 0F) {
                        return@let
                    }
                    val leadGuideLineId = View.generateViewId()
                    constraintSet.create(leadGuideLineId, ConstraintSet.VERTICAL)
                    constraintSet.setGuidelinePercent(leadGuideLineId, it / 100)

                    layoutParams.leftToLeft = leadGuideLineId
                    proportionalImageView.scaleType = ImageView.ScaleType.FIT_START
                }
                positionGuide.trailing?.let {
                    if (it < 0F) {
                        return@let
                    }
                    val trailGuideLineId = View.generateViewId()
                    constraintSet.create(trailGuideLineId, ConstraintSet.VERTICAL)
                    constraintSet.setGuidelinePercent(trailGuideLineId, 1F - (it / 100))

                    layoutParams.rightToRight = trailGuideLineId
                }
                positionGuide.top?.let {
                    if (it < 0F) {
                        return@let
                    }
                    val topGuideLineId = View.generateViewId()
                    constraintSet.create(topGuideLineId, ConstraintSet.HORIZONTAL)
                    constraintSet.setGuidelinePercent(topGuideLineId, it / 100)

                    layoutParams.topToTop = topGuideLineId
                }
                positionGuide.bottom?.let {
                    if (it < 0F) {
                        return@let
                    }
                    val bottomGuideLineId = View.generateViewId()
                    constraintSet.create(bottomGuideLineId, ConstraintSet.HORIZONTAL)
                    constraintSet.setGuidelinePercent(bottomGuideLineId, 1F - (it / 100))

                    layoutParams.bottomToBottom = bottomGuideLineId
                    proportionalImageView.scaleType = ImageView.ScaleType.FIT_END

                }
                positionGuide.vCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.verticalBias = (0.5F + it / 100)
                }
                positionGuide.hCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                    layoutParams.horizontalBias = (0.5F + it / 100)
                }
                constraintSet.applyTo(host)
                proportionalImageView.layoutParams = layoutParams

                host.addView(proportionalImageView)


                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }

        }

    }
}
