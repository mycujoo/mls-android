package tv.mycujoo.mls.helper

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.doOnLayout
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasDynamicIntroAnimation
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasDynamicOutroAnimation
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasStaticIntroAnimation
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasStaticOutroAnimation
import tv.mycujoo.mls.manager.VariableKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.ProportionalImageView
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayViewHelper(
    private val viewHandler: IViewHandler,
    private val overlayFactory: IOverlayFactory,
    private val animationFactory: AnimationFactory,
    private val variableTranslator: VariableTranslator,
    private val variableKeeper: VariableKeeper
) {

    /**region Add view*/
    fun addView(
        context: Context,
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        if (showOverlayAction.introTransitionSpec!!.animationType == AnimationType.NONE) {
            addViewWithNoAnimation(
                context,
                overlayHost,
                showOverlayAction
            )
        } else {
            addViewWithAnimation(
                context,
                overlayHost,
                showOverlayAction
            )
        }
    }
    /**endregion */

    /**region Add view with animation*/
    fun addViewWithAnimation(
        context: Context,
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {
            val scaffoldView =
                overlayFactory.createScaffoldView(
                    context,
                    showOverlayAction,
                    variableTranslator,
                    variableKeeper
                )

            when {
                hasStaticIntroAnimation(showOverlayAction.introTransitionSpec!!.animationType) -> {
                    doAddViewWithStaticAnimation(
                        overlayHost,
                        scaffoldView,
                        showOverlayAction.viewSpec!!.positionGuide!!,
                        showOverlayAction.introTransitionSpec
                    )
                }
                hasDynamicIntroAnimation(showOverlayAction.introTransitionSpec.animationType) -> {
                    doAddViewWithDynamicAnimation(
                        overlayHost,
                        scaffoldView,
                        showOverlayAction.viewSpec!!.positionGuide!!,
                        showOverlayAction.introTransitionSpec
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
        overlayHost: ConstraintLayout,
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
        overlayHost: ConstraintLayout,
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
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        viewHandler.incrementIdlingResource()

        val scaffoldView =
            overlayFactory.createScaffoldView(
                context,
                showOverlayAction,
                variableTranslator,
                variableKeeper
            )

        doAddViewWithNoAnimation(
            overlayHost,
            scaffoldView,
            showOverlayAction.viewSpec!!.positionGuide!!
        )

    }


    private fun doAddViewWithNoAnimation(
        overlayHost: ConstraintLayout,
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
        overlayHost: ConstraintLayout,
        actionId: String,
        outroTransitionSpec: TransitionSpec?
    ) {
        if (outroTransitionSpec == null || outroTransitionSpec.animationType == AnimationType.NONE) {
            removeViewWithNoAnimation(overlayHost, actionId)
        } else {
            removeViewWithAnimation(overlayHost, actionId, outroTransitionSpec)
        }
    }

    fun removeView(
        overlayHost: ConstraintLayout,
        hideOverlayActionEntity: HideOverlayActionEntity
    ) {
        removeViewWithNoAnimation(overlayHost, hideOverlayActionEntity.id)
    }

    private fun removeViewWithNoAnimation(overlayHost: ConstraintLayout, overlayId: String) {
        overlayHost.children.filter { it.tag == overlayId }
            .forEach {
                viewHandler.detachOverlayView(it as ScaffoldView)
            }
        viewHandler.removeAnimation(overlayId)
    }
    /**endregion */


    /**region Remove view with animation*/
    fun removeViewWithAnimation(
        overlayHost: ConstraintLayout,
        actionId: String,
        outroTransitionSpec: TransitionSpec
    ) {
        viewHandler.incrementIdlingResource()

        when {
            hasStaticOutroAnimation(outroTransitionSpec.animationType) -> {
                removeViewWithStaticAnimation(
                    overlayHost,
                    actionId,
                    outroTransitionSpec

                )
            }
            hasDynamicOutroAnimation(outroTransitionSpec.animationType) -> {
                removeViewWithDynamicAnimation(
                    overlayHost,
                    actionId,
                    outroTransitionSpec
                )
            }
            else -> {
                // should not happen
                viewHandler.decrementIdlingResource()
            }
        }
    }

    private fun removeViewWithStaticAnimation(
        overlayHost: ConstraintLayout,
        actionId: String,
        outroTransitionSpec: TransitionSpec
    ) {
        overlayHost.post {
            overlayHost.children.filter { it.tag == actionId }.forEach { view ->
                view as ScaffoldView

                val animation = animationFactory.createRemoveViewStaticAnimation(
                    overlayHost,
                    actionId,
                    outroTransitionSpec,
                    view,
                    viewHandler
                )

                animation.start()
            }
            viewHandler.decrementIdlingResource()
        }
    }

    private fun removeViewWithDynamicAnimation(
        overlayHost: ConstraintLayout,
        actionId: String,
        outroTransitionSpec: TransitionSpec
    ) {
        overlayHost.post {
            overlayHost.children.filter { it.tag == actionId }.forEach { view ->
                view as ScaffoldView

                val animation = animationFactory.createRemoveViewDynamicAnimation(
                    overlayHost,
                    actionId,
                    outroTransitionSpec,
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
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {

            val scaffoldView =
                overlayFactory.createScaffoldView(
                    overlayHost.context,
                    showOverlayAction,
                    variableTranslator,
                    variableKeeper
                )

            scaffoldView.doOnLayout {

                val animation = animationFactory.createLingeringIntroViewAnimation(
                    overlayHost,
                    scaffoldView,
                    showOverlayAction,
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


            val positionGuide = showOverlayAction.viewSpec!!.positionGuide
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
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {
            overlayHost.children.firstOrNull { it.tag == showOverlayAction.id }?.let { view ->

                viewHandler.getAnimationWithTag(showOverlayAction.id)?.let {
                    it.cancel()
                }
                viewHandler.removeAnimation(showOverlayAction.id)

                val scaffoldView = viewHandler.getOverlayView(
                    showOverlayAction.id
                )
                scaffoldView?.let {
                    viewHandler.detachOverlayView(
                        it
                    )
                }


                addLingeringIntroViewWithAnimation(
                    overlayHost,
                    showOverlayAction,
                    animationPosition,
                    isPlaying
                )
            }
            viewHandler.decrementIdlingResource()
        }

    }


    fun addLingeringOutroViewWithAnimation(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayHost.post {

            val scaffoldView =
                overlayFactory.createScaffoldView(
                    overlayHost.context,
                    showOverlayAction,
                    variableTranslator,
                    variableKeeper
                )

            scaffoldView.doOnLayout {

                animationFactory.createLingeringOutroAnimation(
                    overlayHost,
                    scaffoldView,
                    showOverlayAction,
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
            val positionGuide = showOverlayAction.viewSpec?.positionGuide
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
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        val scaffoldView = viewHandler.getOverlayView(showOverlayAction.id) ?: return

        overlayHost.post {
            viewHandler.getAnimationWithTag(showOverlayAction.id)?.let {
                it.cancel()
            }
            viewHandler.removeAnimation(showOverlayAction.id)

            viewHandler.detachOverlayView(
                scaffoldView
            )

            addLingeringOutroViewWithAnimation(
                overlayHost,
                showOverlayAction,
                animationPosition,
                isPlaying
            )
        }

    }

    fun updateLingeringMidwayOverlay(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        val scaffoldView = viewHandler.getOverlayView(showOverlayAction.id) ?: return

        overlayHost.post {
            val constraintSet = ConstraintSet()
            constraintSet.clone(overlayHost)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            val positionGuide = showOverlayAction.viewSpec!!.positionGuide!!

            applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)


            scaffoldView.layoutParams = layoutParams


            scaffoldView.visibility = View.VISIBLE
            constraintSet.applyTo(overlayHost)
        }

    }


    /**endregion */

    /**region Add or Update lingering intro view*/
    fun addOrUpdateLingeringIntroOverlay(
        tvOverlayContainer: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        if (viewHandler.overlayIsAttached(showOverlayAction.id)) {
            updateLingeringIntroOverlay(
                tvOverlayContainer,
                showOverlayAction,
                animationPosition,
                isPlaying
            )
        } else {
            addLingeringIntroViewWithAnimation(
                tvOverlayContainer,
                showOverlayAction,
                animationPosition,
                isPlaying
            )
        }
    }
    /**endregion */

    /**region Add or Update lingering outro view*/
    fun addOrUpdateLingeringOutroOverlay(
        tvOverlayContainer: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        if (viewHandler.overlayIsAttached(showOverlayAction.id)) {
            updateLingeringOutroOverlay(
                tvOverlayContainer,
                showOverlayAction,
                animationPosition,
                isPlaying
            )
        } else {
            addLingeringOutroViewWithAnimation(
                tvOverlayContainer,
                showOverlayAction,
                animationPosition,
                isPlaying
            )
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

    /**region Variables*/
    fun setVariable(variable: VariableEntity) {
        variableTranslator.createVariableTripleIfNotExisted(variable.variable.name)
        variableTranslator
            .emitNewValue(variable.variable.name, variable.variable.printValue())
    }

    fun incrementVariable(incrementVariableEntity: IncrementVariableEntity) {
        variableTranslator.getValue(incrementVariableEntity.name)
            ?.let { currentValue ->
                val newValue: Any
                newValue = when (currentValue) {
                    is Double -> {
                        currentValue + (incrementVariableEntity.amount as Double)
                    }
                    is Long -> {
                        currentValue + (incrementVariableEntity.amount as Long)
                    }
                    else -> {
                        // should not happen
                        ""
                    }
                }
                variableTranslator
                    .emitNewValue(incrementVariableEntity.name, newValue)
            }
    }
    /**endregion */
}