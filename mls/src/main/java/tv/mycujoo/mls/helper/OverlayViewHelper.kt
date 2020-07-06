package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.ShowOverlayActionEntity
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView

class OverlayViewHelper {
    companion object {

        fun addView(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            overlayEntity: ShowOverlayActionEntity,
            objectAnimator: ObjectAnimator?,
            idlingResource: CountingIdlingResource
        ) {
            when (overlayEntity.animationType) {
                NONE -> {
                    addViewWithNoAnimation(
                        host,
                        proportionalImageView,
                        positionGuide,
                        overlayEntity,
                        idlingResource
                    )
                }
                FADE_IN -> {
                    addViewWithStaticAnimation(
                        host,
                        proportionalImageView,
                        positionGuide,
                        overlayEntity,
                        objectAnimator,
                        idlingResource
                    )
                }
                FADE_OUT -> {
                    // should not happen
                }
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING -> {
                    addViewWithDynamicAnimation(
                        host,
                        proportionalImageView,
                        positionGuide,
                        overlayEntity,
                        objectAnimator,
                        idlingResource
                    )
                }
            }


        }


        fun addViewWithNoAnimation(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            overlayEntity: ShowOverlayActionEntity,
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
                    setLeadingConstraints(constraintSet, it, layoutParams, proportionalImageView)
                }
                positionGuide.trailing?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setTrailingConstraints(constraintSet, it, layoutParams)
                }
                positionGuide.top?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setTopConstraints(constraintSet, it, layoutParams)
                }
                positionGuide.bottom?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setBottomConstraints(constraintSet, it, layoutParams, proportionalImageView)
                }
                positionGuide.vCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    setVCenterConstraints(layoutParams, it)
                }
                positionGuide.hCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    setHCenterConstrains(layoutParams, it)
                }

                constraintSet.applyTo(host)
                proportionalImageView.layoutParams = layoutParams

                host.addView(proportionalImageView)


                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }

        }


        private fun addViewWithStaticAnimation(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            overlayEntity: ShowOverlayActionEntity,
            objectAnimator: ObjectAnimator?,
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
                    setLeadingConstraints(constraintSet, it, layoutParams, proportionalImageView)
                }
                positionGuide.trailing?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setTrailingConstraints(constraintSet, it, layoutParams)
                }
                positionGuide.top?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setTopConstraints(constraintSet, it, layoutParams)
                }
                positionGuide.bottom?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setBottomConstraints(constraintSet, it, layoutParams, proportionalImageView)
                }
                positionGuide.vCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    setVCenterConstraints(layoutParams, it)
                }
                positionGuide.hCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    setHCenterConstrains(layoutParams, it)
                }
                constraintSet.applyTo(host)
                proportionalImageView.layoutParams = layoutParams

                host.addView(proportionalImageView)
                objectAnimator?.start()


                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }

        }

        private fun addViewWithDynamicAnimation(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            overlayEntity: ShowOverlayActionEntity,
            objectAnimator: ObjectAnimator?,
            idlingResource: CountingIdlingResource
        ) {

            host.post {

                (host as ViewGroup).setOnHierarchyChangeListener(object :
                    ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewRemoved(parent: View?, child: View?) {

                    }

                    override fun onChildViewAdded(parent: View?, child: View?) {
                        if (child != null && child.id == proportionalImageView.id) {
                            host.post {


                                val x = proportionalImageView.x
                                val y = proportionalImageView.y

                                when (overlayEntity.animationType) {
                                    NONE,
                                    FADE_OUT,
                                    FADE_IN -> {
                                        // should not happen
                                    }
                                    SLIDE_FROM_LEADING -> {
                                        proportionalImageView.x =
                                            -proportionalImageView.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            x
                                        )
                                        animation.duration = 2000L
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {

                                            }

                                            override fun onAnimationCancel(animation: Animator?) {

                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE
                                            }

                                        })
                                        animation.start()
                                    }
                                    SLIDE_FROM_TRAILING -> {
                                        proportionalImageView.x = host.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            x
                                        )
                                        animation.duration = 2000L
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {

                                            }

                                            override fun onAnimationCancel(animation: Animator?) {

                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE

                                            }

                                        })
                                        animation.start()
                                    }
                                }

                            }
                        }

                    }

                })

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
                    setLeadingConstraints(constraintSet, it, layoutParams, proportionalImageView)
                }
                positionGuide.trailing?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setTrailingConstraints(constraintSet, it, layoutParams)
                }
                positionGuide.top?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setTopConstraints(constraintSet, it, layoutParams)
                }
                positionGuide.bottom?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setBottomConstraints(constraintSet, it, layoutParams, proportionalImageView)
                }
                positionGuide.vCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    setVCenterConstraints(layoutParams, it)
                }
                positionGuide.hCenter?.let {
                    if (it > 50F || it < -50F) {
                        return@let
                    }
                    setHCenterConstrains(layoutParams, it)
                }

                proportionalImageView.layoutParams = layoutParams
                proportionalImageView.visibility = View.INVISIBLE
                constraintSet.applyTo(host)
                host.addView(proportionalImageView)

                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }

            }

        }


        private fun setHCenterConstrains(
            layoutParams: ConstraintLayout.LayoutParams,
            it: Float
        ) {
            layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.horizontalBias = (0.5F + it / 100)
        }

        private fun setVCenterConstraints(
            layoutParams: ConstraintLayout.LayoutParams,
            it: Float
        ) {
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.verticalBias = (0.5F + it / 100)
        }

        private fun setBottomConstraints(
            constraintSet: ConstraintSet,
            it: Float,
            layoutParams: ConstraintLayout.LayoutParams,
            proportionalImageView: ProportionalImageView
        ) {
            val bottomGuideLineId = View.generateViewId()
            constraintSet.create(bottomGuideLineId, ConstraintSet.HORIZONTAL)
            constraintSet.setGuidelinePercent(bottomGuideLineId, 1F - (it / 100))

            layoutParams.bottomToBottom = bottomGuideLineId
            proportionalImageView.scaleType = ImageView.ScaleType.FIT_END
        }

        private fun setTopConstraints(
            constraintSet: ConstraintSet,
            it: Float,
            layoutParams: ConstraintLayout.LayoutParams
        ) {
            val topGuideLineId = View.generateViewId()
            constraintSet.create(topGuideLineId, ConstraintSet.HORIZONTAL)
            constraintSet.setGuidelinePercent(topGuideLineId, it / 100)

            layoutParams.topToTop = topGuideLineId
        }

        private fun setTrailingConstraints(
            constraintSet: ConstraintSet,
            it: Float,
            layoutParams: ConstraintLayout.LayoutParams
        ) {
            val trailGuideLineId = View.generateViewId()
            constraintSet.create(trailGuideLineId, ConstraintSet.VERTICAL)
            constraintSet.setGuidelinePercent(trailGuideLineId, 1F - (it / 100))

            layoutParams.rightToRight = trailGuideLineId
        }

        private fun setLeadingConstraints(
            constraintSet: ConstraintSet,
            it: Float,
            layoutParams: ConstraintLayout.LayoutParams,
            proportionalImageView: ProportionalImageView
        ) {
            val leadGuideLineId = View.generateViewId()
            constraintSet.create(leadGuideLineId, ConstraintSet.VERTICAL)
            constraintSet.setGuidelinePercent(leadGuideLineId, it / 100)

            layoutParams.leftToLeft = leadGuideLineId
            proportionalImageView.scaleType = ImageView.ScaleType.FIT_START
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

    }
}
