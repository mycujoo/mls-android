package tv.mycujoo.mls.helper

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.doOnLayout
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayViewHelper(private val animationHelper: AnimationHelper) {

    /**region With animation*/
    fun addViewWithAnimation(
        context: Context,
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        viewIdentifierManager: ViewIdentifierManager
    ) {
        viewIdentifierManager.idlingResource.increment()

        overlayHost.post {
            val scaffoldView =
                OverlayFactory.createScaffoldView(
                    context,
                    overlayEntity,
                    viewIdentifierManager.variableTranslator,
                    viewIdentifierManager.timeKeeper
                )

            when (overlayEntity.introTransitionSpec.animationType) {
                AnimationType.FADE_IN -> {
                    doAddViewWithStaticAnimation(
                        overlayHost,
                        scaffoldView,
                        overlayEntity.viewSpec.positionGuide!!,
                        overlayEntity.introTransitionSpec,
                        viewIdentifierManager
                    )
                }
                AnimationType.SLIDE_FROM_LEFT,
                AnimationType.SLIDE_FROM_RIGHT -> {

                    doAddViewWithDynamicAnimation(
                        overlayHost,
                        scaffoldView,
                        overlayEntity.viewSpec.positionGuide!!,
                        overlayEntity.introTransitionSpec,
                        viewIdentifierManager
                    )
                }
                else -> {
                    // should not happen
                }
            }

        }

    }

    private fun doAddViewWithDynamicAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        positionGuide: PositionGuide,
        introTransitionSpec: TransitionSpec,
        viewIdentifierManager: ViewIdentifierManager
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(overlayHost)
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        applyPositionGuide(
            positionGuide,
            constraintSet,
            layoutParams,
            scaffoldView
        )

        scaffoldView.layoutParams = layoutParams
        scaffoldView.visibility = View.INVISIBLE
        constraintSet.applyTo(overlayHost)
        overlayHost.addView(scaffoldView)
        viewIdentifierManager.attachOverlayView(scaffoldView)

        scaffoldView.doOnLayout {

            val anim = animationHelper.createAddViewDynamicAnimation(
                overlayHost,
                scaffoldView,
                introTransitionSpec,
                viewIdentifierManager
            )
            anim?.start()
            if (!viewIdentifierManager.idlingResource.isIdleNow) {
                viewIdentifierManager.idlingResource.decrement()
            }
        }

    }

    private fun doAddViewWithStaticAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        positionGuide: PositionGuide,
        introTransitionSpec: TransitionSpec,
        viewIdentifierManager: ViewIdentifierManager
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(overlayHost)
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        applyPositionGuide(
            positionGuide,
            constraintSet,
            layoutParams,
            scaffoldView
        )


        constraintSet.applyTo(overlayHost)
        scaffoldView.layoutParams = layoutParams

        overlayHost.addView(scaffoldView)
        viewIdentifierManager.attachOverlayView(scaffoldView)

        val animation = animationHelper.createStaticAnimation(
            scaffoldView,
            introTransitionSpec.animationType,
            introTransitionSpec.animationDuration
        )
        viewIdentifierManager.addAnimation(scaffoldView.tag as String, animation!!)
        animation.start()
        if (!viewIdentifierManager.idlingResource.isIdleNow) {
            viewIdentifierManager.idlingResource.decrement()
        }

    }
    /**endregion */


    /**region With no animation*/
    fun addViewWithNoAnimation(
        context: Context,
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        viewIdentifierManager: ViewIdentifierManager
    ) {
        viewIdentifierManager.idlingResource.increment()

        val scaffoldView =
            OverlayFactory.createScaffoldView(
                context,
                overlayEntity,
                viewIdentifierManager.variableTranslator,
                viewIdentifierManager.timeKeeper
            )

        doAddViewWithNoAnimation(
            overlayHost,
            scaffoldView,
            overlayEntity.viewSpec.positionGuide!!,
            viewIdentifierManager
        )

    }


    private fun doAddViewWithNoAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        positionGuide: PositionGuide,
        viewIdentifierManager: ViewIdentifierManager
    ) {
        overlayHost.post {
            val constraintSet = ConstraintSet()
            constraintSet.clone(overlayHost)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            applyPositionGuide(
                positionGuide,
                constraintSet,
                layoutParams,
                scaffoldView
            )


            constraintSet.applyTo(overlayHost)
            scaffoldView.layoutParams = layoutParams

            overlayHost.addView(scaffoldView)
            viewIdentifierManager.attachOverlayView(scaffoldView)
            scaffoldView.doOnLayout {
                if (!viewIdentifierManager.idlingResource.isIdleNow) {
                    viewIdentifierManager.idlingResource.decrement()
                }
            }
        }

    }
    /**endregion */


    /**region Positioning Functions*/
    private fun applyPositionGuide(
        positionGuide: PositionGuide,
        constraintSet: ConstraintSet,
        layoutParams: ConstraintLayout.LayoutParams,
        scaffoldView: ScaffoldView
    ) {

        positionGuide.left?.let {
            if (it < 0F) {
                return@let
            }
            setLeftConstraints(
                constraintSet,
                it,
                layoutParams,
                scaffoldView
            )
        }
        positionGuide.right?.let {
            if (it < 0F) {
                return@let
            }
            setRightConstraints(constraintSet, it, layoutParams)
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
            setBottomConstraints(
                constraintSet,
                it,
                layoutParams,
                scaffoldView
            )
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
        proportionalImageView: View
    ) {
        val bottomGuideLineId = View.generateViewId()
        constraintSet.create(bottomGuideLineId, ConstraintSet.HORIZONTAL)
        constraintSet.setGuidelinePercent(bottomGuideLineId, 1F - (it / 100))

        layoutParams.bottomToBottom = bottomGuideLineId
        if (proportionalImageView is ProportionalImageView) {
            proportionalImageView.scaleType = ImageView.ScaleType.FIT_END
        } else {
            (proportionalImageView as ScaffoldView).setScaleType(ImageView.ScaleType.FIT_END)
        }
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

    private fun setRightConstraints(
        constraintSet: ConstraintSet,
        it: Float,
        layoutParams: ConstraintLayout.LayoutParams
    ) {
        val rightGuideLineId = View.generateViewId()
        constraintSet.create(rightGuideLineId, ConstraintSet.VERTICAL)
        constraintSet.setGuidelinePercent(rightGuideLineId, 1F - (it / 100))

        layoutParams.rightToRight = rightGuideLineId
    }

    private fun setLeftConstraints(
        constraintSet: ConstraintSet,
        it: Float,
        layoutParams: ConstraintLayout.LayoutParams,
        proportionalImageView: View
    ) {
        val leftGuideLineId = View.generateViewId()
        constraintSet.create(leftGuideLineId, ConstraintSet.VERTICAL)
        constraintSet.setGuidelinePercent(leftGuideLineId, it / 100)

        layoutParams.leftToLeft = leftGuideLineId
        if (proportionalImageView is ProportionalImageView) {
            proportionalImageView.scaleType = ImageView.ScaleType.FIT_START
        } else {
            (proportionalImageView as ScaffoldView).setScaleType(ImageView.ScaleType.FIT_START)
        }
    }


    /**endregion */
}