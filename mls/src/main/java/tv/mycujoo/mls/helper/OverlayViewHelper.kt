package tv.mycujoo.mls.helper

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.doOnLayout
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.TransitionSpec
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasDynamicIntroAnimation
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasDynamicOutroAnimation
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasStaticIntroAnimation
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasStaticOutroAnimation
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayViewHelper(
    private val viewHandler: IViewHandler,
    private val overlayFactory : IOverlayFactory,
    private val animationFactory: AnimationFactory
) {

    /**region Add view*/
    fun addView(
        context: Context,
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        if (overlayEntity.introTransitionSpec.animationType == AnimationType.NONE ||
            overlayEntity.introTransitionSpec.animationType == AnimationType.UNSPECIFIED
        ) {
            addViewWithNoAnimation(
                context,
                overlayHost,
                overlayEntity
            )
        } else {
            addViewWithAnimation(
                context,
                overlayHost,
                overlayEntity
            )
        }
    }
    /**endregion */

    /**region Add view with animation*/
    fun addViewWithAnimation(
        context: Context,
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {
            val scaffoldView =
                overlayFactory.createScaffoldView(
                    context,
                    overlayEntity,
                    viewHandler.getVariableTranslator(),
                    viewHandler.getTimerKeeper()
                )

            when {
                hasStaticIntroAnimation(overlayEntity.introTransitionSpec.animationType) -> {
                    doAddViewWithStaticAnimation(
                        overlayHost,
                        scaffoldView,
                        overlayEntity.viewSpec.positionGuide!!,
                        overlayEntity.introTransitionSpec
                    )
                }
                hasDynamicIntroAnimation(overlayEntity.introTransitionSpec.animationType) -> {
                    doAddViewWithDynamicAnimation(
                        overlayHost,
                        scaffoldView,
                        overlayEntity.viewSpec.positionGuide!!,
                        overlayEntity.introTransitionSpec
                    )
                }
                else -> {
                    // should not happen
                    viewHandler.decrementIdlingResource()
                }
            }
        }

    }

    private fun doAddViewWithDynamicAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        positionGuide: PositionGuide,
        introTransitionSpec: TransitionSpec
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
        viewHandler.attachOverlayView(scaffoldView)

        scaffoldView.doOnLayout {

            val anim = animationFactory.createAddViewDynamicAnimation(
                overlayHost,
                scaffoldView,
                introTransitionSpec,
                viewHandler
            )
            anim?.start()
            viewHandler.decrementIdlingResource()
        }

    }

    private fun doAddViewWithStaticAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        positionGuide: PositionGuide,
        introTransitionSpec: TransitionSpec
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

        viewHandler.attachOverlayView(scaffoldView)

        val animation = animationFactory.createAddViewStaticAnimation(
            scaffoldView,
            introTransitionSpec.animationType,
            introTransitionSpec.animationDuration
        )
        viewHandler.addAnimation(scaffoldView.tag as String, animation!!)
        animation.start()
        viewHandler.decrementIdlingResource()
    }
    /**endregion */


    /**region Add view with NO animation*/
    fun addViewWithNoAnimation(
        context: Context,
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        viewHandler.incrementIdlingResource()

        val scaffoldView =
            overlayFactory.createScaffoldView(
                context,
                overlayEntity,
                viewHandler.getVariableTranslator(),
                viewHandler.getTimerKeeper()
            )

        doAddViewWithNoAnimation(
            overlayHost,
            scaffoldView,
            overlayEntity.viewSpec.positionGuide!!
        )

    }


    private fun doAddViewWithNoAnimation(
        overlayHost: OverlayHost,
        scaffoldView: ScaffoldView,
        positionGuide: PositionGuide
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

            viewHandler.attachOverlayView(scaffoldView)
            scaffoldView.doOnLayout {
                viewHandler.decrementIdlingResource()
            }
        }

    }
    /**endregion */

    /**region Remove view*/
    fun removeView(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        if (overlayEntity.outroTransitionSpec.animationType == AnimationType.NONE ||
            overlayEntity.outroTransitionSpec.animationType == AnimationType.UNSPECIFIED
        ) {
            removeViewWithNoAnimation(overlayHost, overlayEntity)
        } else {
            removeViewWithAnimation(overlayHost, overlayEntity)
        }
    }

    private fun removeViewWithNoAnimation(overlayHost: OverlayHost, overlayEntity: OverlayEntity) {
        overlayHost.children.filter { it.tag == overlayEntity.id }
            .forEach {
                viewHandler.detachOverlayView(it as ScaffoldView)
            }
        viewHandler.removeAnimation(overlayEntity.id)
    }
    /**endregion */


    /**region Remove view with animation*/
    fun removeViewWithAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        viewHandler.incrementIdlingResource()

        when {
            hasStaticOutroAnimation(overlayEntity.outroTransitionSpec.animationType) -> {
                removeViewWithStaticAnimation(
                    overlayHost,
                    overlayEntity
                )
            }
            hasDynamicOutroAnimation(overlayEntity.outroTransitionSpec.animationType) -> {
                removeViewWithDynamicAnimation(
                    overlayHost,
                    overlayEntity
                )
            }
            else -> {
                // should not happen
                viewHandler.decrementIdlingResource()
            }
        }
    }

    private fun removeViewWithStaticAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        overlayHost.post {
            overlayHost.children.filter { it.tag == overlayEntity.id }.forEach { view ->
                view as ScaffoldView

                val animation = animationFactory.createRemoveViewStaticAnimation(
                    overlayHost,
                    overlayEntity,
                    view,
                    viewHandler
                )

                animation.start()
            }
            viewHandler.decrementIdlingResource()
        }
    }

    private fun removeViewWithDynamicAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        overlayHost.post {
            overlayHost.children.filter { it.tag == overlayEntity.id }.forEach { view ->
                view as ScaffoldView


                val animation = animationFactory.createRemoveViewDynamicAnimation(
                    overlayHost,
                    overlayEntity,
                    view,
                    viewHandler
                )

                if (animation == null) {
                    // should not happen
                    Log.e("OverlayEntityView", "animation must not be null")
                    return@forEach
                }

                animation.start()
            }

            viewHandler.decrementIdlingResource()
        }
    }


    /**endregion */


    /**region Add lingering view with animation*/
    fun addLingeringIntroViewWithAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {

            val scaffoldView =
                overlayFactory.createScaffoldView(
                    overlayHost.context,
                    overlayEntity,
                    viewHandler.getVariableTranslator(),
                    viewHandler.getTimerKeeper()
                )

            scaffoldView.doOnLayout {

                val animation = animationFactory.createLingeringIntroViewAnimation(
                    overlayHost,
                    scaffoldView,
                    overlayEntity,
                    animationPosition,
                    isPlaying,
                    viewHandler
                )

                if (animation == null) {
                    Log.e("OverlayEntityView", "animation must not be null")
                    return@doOnLayout
                }
                viewHandler.decrementIdlingResource()

            }

            val constraintSet = ConstraintSet()
            constraintSet.clone(overlayHost)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )


            val positionGuide = overlayEntity.viewSpec.positionGuide
            if (positionGuide == null) {
                // should not happen
                Log.e("OverlayEntityView", "animation must not be null")
                return@post
            }


            applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)


            scaffoldView.layoutParams = layoutParams
            scaffoldView.visibility = View.INVISIBLE
            constraintSet.applyTo(overlayHost)
            viewHandler.attachOverlayView(scaffoldView)

        }


    }

    fun updateLingeringIntroOverlay(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {
            overlayHost.children.firstOrNull { it.tag == overlayEntity.id }?.let { view ->

                viewHandler.getAnimationWithTag(overlayEntity.id)?.let {
                    it.cancel()
                }
                viewHandler.removeAnimation(overlayEntity.id)

                val scaffoldView = viewHandler.getOverlayView(
                    overlayEntity.id
                )
                scaffoldView?.let {
                    viewHandler.detachOverlayView(
                        it
                    )
                }


                addLingeringIntroViewWithAnimation(
                    overlayHost,
                    overlayEntity,
                    animationPosition,
                    isPlaying
                )
            }
            viewHandler.decrementIdlingResource()
        }

    }


    fun addLingeringOutroViewWithAnimation(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayHost.post {

            val scaffoldView =
                overlayFactory.createScaffoldView(
                    overlayHost.context,
                    overlayEntity,
                    viewHandler.getVariableTranslator(),
                    viewHandler.getTimerKeeper()
                )

            scaffoldView.doOnLayout {

                animationFactory.createLingeringOutroAnimation(
                    overlayHost,
                    scaffoldView,
                    overlayEntity,
                    animationPosition,
                    isPlaying,
                    viewHandler
                )

            }

            val constraintSet = ConstraintSet()
            constraintSet.clone(overlayHost)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            val positionGuide = overlayEntity.viewSpec.positionGuide
            if (positionGuide == null) {
                // should not happen
                return@post
            }

            applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)

            scaffoldView.layoutParams = layoutParams
            scaffoldView.visibility = View.INVISIBLE
            constraintSet.applyTo(overlayHost)
            viewHandler.attachOverlayView(scaffoldView)

        }


    }

    fun updateLingeringOutroOverlay(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        val scaffoldView = viewHandler.getOverlayView(overlayEntity.id) ?: return

        overlayHost.post {
            viewHandler.getAnimationWithTag(overlayEntity.id)?.let {
                it.cancel()
            }
            viewHandler.removeAnimation(overlayEntity.id)

            viewHandler.detachOverlayView(
                scaffoldView
            )

            addLingeringOutroViewWithAnimation(
                overlayHost,
                overlayEntity,
                animationPosition,
                isPlaying
            )
        }

    }

    fun updateLingeringMidwayOverlay(
        overlayHost: OverlayHost,
        overlayEntity: OverlayEntity
    ) {
        val scaffoldView = viewHandler.getOverlayView(overlayEntity.id) ?: return

        overlayHost.post {
            val constraintSet = ConstraintSet()
            constraintSet.clone(overlayHost)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            val positionGuide = overlayEntity.viewSpec.positionGuide!!

            applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)


            scaffoldView.layoutParams = layoutParams


            scaffoldView.visibility = View.VISIBLE
            constraintSet.applyTo(overlayHost)
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