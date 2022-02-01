package tv.mycujoo.mcls.helper

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.doOnLayout
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.helper.AnimationClassifierHelper.Companion.hasDynamicIntroAnimation
import tv.mycujoo.mcls.helper.AnimationClassifierHelper.Companion.hasDynamicOutroAnimation
import tv.mycujoo.mcls.helper.AnimationClassifierHelper.Companion.hasStaticIntroAnimation
import tv.mycujoo.mcls.helper.AnimationClassifierHelper.Companion.hasStaticOutroAnimation
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.widgets.ProportionalImageView
import tv.mycujoo.mcls.widgets.ScaffoldView
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helps with View related operations. i.e. Add/Remove overlay view to/from screen.
 *
 */
@Singleton
class OverlayViewHelper @Inject constructor(
    private val viewHandler: IViewHandler,
    private val overlayFactory: IOverlayFactory,
    private val animationFactory: AnimationFactory
) {

    /**region Add view*/
    /**
     * Add overlay view to host view with specified animation. If there is a specified animation, it will be add with amimation,
     * otherwise no animation will be created.
     * This is the only method that should be used for 'Adding' overlay. All other methods are private and considered internal service.
     * @param overlayHost to host to overlay view
     * @param showOverlayAction data needed for type of animation and overlay
     */
    fun addView(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        if (showOverlayAction.introTransitionSpec?.animationType == AnimationType.NONE) {
            addViewWithNoAnimation(
                overlayHost,
                showOverlayAction
            )
        } else {
            addViewWithAnimation(
                overlayHost,
                showOverlayAction
            )
        }
    }
    /**endregion */

    /**region Add view with animation*/
    private fun addViewWithAnimation(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {
            val scaffoldView = overlayFactory.createScaffoldView(showOverlayAction)

            val positionGuide = showOverlayAction.viewSpec?.positionGuide ?: PositionGuide(
                left = 0f,
                top = 0f
            )
            val animationType =
                showOverlayAction.introTransitionSpec?.animationType ?: AnimationType.NONE

            val introTransitionSpec = showOverlayAction.introTransitionSpec ?: TransitionSpec(
                offset = 0,
                animationType = animationType,
                animationDuration = 0
            )

            when {
                hasStaticIntroAnimation(animationType) -> {
                    doAddViewWithStaticAnimation(
                        overlayHost,
                        scaffoldView,
                        positionGuide,
                        introTransitionSpec
                    )
                }
                hasDynamicIntroAnimation(animationType) -> {
                    doAddViewWithDynamicAnimation(
                        overlayHost,
                        scaffoldView,
                        positionGuide,
                        introTransitionSpec
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
        animation?.let {
            viewHandler.addAnimation(scaffoldView.tag as String, animation)
            animation.start()
            viewHandler.decrementIdlingResource()
        }
    }
    /**endregion */


    /**region Add view with NO animation*/
    private fun addViewWithNoAnimation(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        viewHandler.incrementIdlingResource()

        val scaffoldView = overlayFactory.createScaffoldView(showOverlayAction)

        val positionGuide = showOverlayAction.viewSpec?.positionGuide ?: PositionGuide(
            left = 0f,
            top = 0f
        )

        doAddViewWithNoAnimation(
            overlayHost,
            scaffoldView,
            positionGuide
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
    /**
     * Remove overlay view from host view with specified animation.
     * @param overlayHost the host that overlay view is displayed in currently
     * @param customId overlay identifier (view tag)
     * @param outroTransitionSpec transition specification needed for outro animation type and duration
     */
    fun removeView(
        overlayHost: ConstraintLayout,
        customId: String,
        outroTransitionSpec: TransitionSpec?
    ) {
        if (outroTransitionSpec == null || outroTransitionSpec.animationType == AnimationType.NONE) {
            removeViewWithNoAnimation(overlayHost, customId)
        } else {
            removeViewWithAnimation(overlayHost, customId, outroTransitionSpec)
        }
    }

    /**
     * Remove overlay view from host view with specified animation.
     * @param overlayHost the host that overlay view is displayed in currently
     * @param hideOverlayActionEntity hide overlay Action which provides customId for removing overlay view with same identifier as 'Tag'
     */
    fun removeView(
        overlayHost: ConstraintLayout,
        hideOverlayActionEntity: HideOverlayActionEntity
    ) {
        removeViewWithNoAnimation(overlayHost, hideOverlayActionEntity.customId)
    }

    fun clearScreen() {
        viewHandler.clearAll()
    }

    private fun removeViewWithNoAnimation(overlayHost: ConstraintLayout, overlayTag: String) {
        overlayHost.children.filter { it.tag == overlayTag }
            .forEach {
                viewHandler.detachOverlayView(it as ScaffoldView)
            }
        viewHandler.removeAnimation(overlayTag)
    }
    /**endregion */


    /**region Remove view with animation*/
    private fun removeViewWithAnimation(
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
    private fun addLingeringIntroViewWithAnimation(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {

            val scaffoldView = overlayFactory.createScaffoldView(showOverlayAction)

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

            val positionGuide = showOverlayAction.viewSpec?.positionGuide ?: PositionGuide(
                left = 0f,
                top = 0f
            )


            applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)


            scaffoldView.layoutParams = layoutParams
            scaffoldView.visibility = View.INVISIBLE
            constraintSet.applyTo(overlayHost)
            viewHandler.attachOverlayView(scaffoldView)

        }


    }

    private fun updateLingeringIntroOverlay(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        viewHandler.incrementIdlingResource()

        overlayHost.post {
            overlayHost.children.firstOrNull { it.tag == showOverlayAction.customId }?.let { _ ->

                viewHandler.getAnimationWithTag(showOverlayAction.customId)?.cancel()
                viewHandler.removeAnimation(showOverlayAction.customId)

                val scaffoldView = viewHandler.getOverlayView(showOverlayAction.customId)

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


    private fun addLingeringOutroViewWithAnimation(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        overlayHost.post {

            val scaffoldView = overlayFactory.createScaffoldView(showOverlayAction)

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
                ?: // should not happen
                return@post

            applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)

            scaffoldView.layoutParams = layoutParams
            scaffoldView.visibility = View.INVISIBLE
            constraintSet.applyTo(overlayHost)
            viewHandler.attachOverlayView(scaffoldView)

        }


    }

    private fun updateLingeringOutroOverlay(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        val scaffoldView = viewHandler.getOverlayView(showOverlayAction.customId) ?: return

        overlayHost.post {
            viewHandler.getAnimationWithTag(showOverlayAction.customId)?.cancel()
            viewHandler.removeAnimation(showOverlayAction.customId)

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

    fun addOrUpdateLingeringMidwayOverlay(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction
    ) {
        if (viewHandler.overlayIsAttached(showOverlayAction.customId)) {
            val scaffoldView = viewHandler.getOverlayView(showOverlayAction.customId) ?: return
            overlayHost.post {
                updateLingeringMidway(overlayHost, showOverlayAction, scaffoldView)
            }
        } else {
            overlayHost.post {
                addViewWithNoAnimation(
                    overlayHost, showOverlayAction
                )
            }
        }
    }

    private fun updateLingeringMidway(
        overlayHost: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        scaffoldView: ScaffoldView
    ) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(overlayHost)
        val layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val positionGuide = showOverlayAction.viewSpec?.positionGuide ?: PositionGuide(
            left = 0f,
            top = 0f
        )

        applyPositionGuide(positionGuide, constraintSet, layoutParams, scaffoldView)


        scaffoldView.layoutParams = layoutParams


        scaffoldView.visibility = View.VISIBLE
        constraintSet.applyTo(overlayHost)
    }


    /**endregion */

    /**region Add or Update lingering intro view*/
    fun addOrUpdateLingeringIntroOverlay(
        tvOverlayContainer: ConstraintLayout,
        showOverlayAction: Action.ShowOverlayAction,
        animationPosition: Long,
        isPlaying: Boolean
    ) {
        if (viewHandler.overlayIsAttached(showOverlayAction.customId)) {
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
        if (viewHandler.overlayIsAttached(showOverlayAction.customId)) {
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
    /**
     * This region provides methods to position a view (overlay view in this case) in a host view,
     * based on Position Guide provided.
     *
     * @param positionGuide guides how a view should be positioned on screen.
     * i.e. distance to left/right/top/bottom axis of screen in percentage.
     *
     * @param constraintSet current constraint set of host view. which should be preserved, but can be over-written.
     * @param layoutParams layout params needed for positioning the view
     * @param scaffoldView the overlay view itself which is going to be positioned
     */
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