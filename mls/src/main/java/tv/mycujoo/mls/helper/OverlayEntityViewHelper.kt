package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.core.view.doOnLayout
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayEntityViewHelper {
    companion object {

        /**region Private Functions*/

        private fun hasNoAnimation(overlayEntity: HideOverlayActionEntity): Boolean {
            return overlayEntity.outroAnimationType == NONE
        }

        private fun hasNoAnimation(overlayEntity: ShowOverlayActionEntity): Boolean {
            return overlayEntity.introAnimationType == NONE
        }

        private fun hasDynamicIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                SLIDE_FROM_LEFT,
                SLIDE_FROM_RIGHT -> {
                    true
                }
                else -> {
                    false
                }
            }
        }

        private fun hasStaticIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                FADE_IN -> {
                    true
                }

                else -> {
                    false
                }
            }
        }

        private fun hasDynamicOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                SLIDE_TO_LEFT,
                SLIDE_TO_RIGHT -> {
                    true
                }

                else -> {
                    false
                }
            }
        }

        private fun hasStaticOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                FADE_OUT -> {
                    true
                }

                else -> {
                    false
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

        fun addViewWithNoAnimation(
            context: Context,
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {
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

                constraintSet.applyTo(overlayHost)
                scaffoldView.layoutParams = layoutParams

                overlayHost.addView(scaffoldView)
                viewIdentifierManager.attachOverlayView(scaffoldView)
            }
        }


        fun addViewWithAnimation(
            context: Context,
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {

            overlayHost.post {
                val scaffoldView =
                    OverlayFactory.createScaffoldView(
                        context,
                        overlayEntity,
                        viewIdentifierManager.variableTranslator,
                        viewIdentifierManager.timeKeeper
                    )

                when (overlayEntity.introTransitionSpec.animationType) {
                    FADE_IN -> {
                        doAddViewWithStaticAnimation(
                            overlayHost,
                            scaffoldView,
                            overlayEntity.viewSpec.positionGuide!!,
                            overlayEntity.introTransitionSpec,
                            viewIdentifierManager
                        )
                    }
                    SLIDE_FROM_LEFT,
                    SLIDE_FROM_RIGHT -> {

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

        private fun doAddViewWithStaticAnimation(
            overlayHost: OverlayHost,
            scaffoldView: ScaffoldView,
            positionGuide: PositionGuide,
            introTransitionSpec: TransitionSpec,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {

                val constraintSet = ConstraintSet()
                constraintSet.clone(overlayHost)
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

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
                constraintSet.applyTo(overlayHost)
                scaffoldView.layoutParams = layoutParams

                overlayHost.addView(scaffoldView)
                viewIdentifierManager.attachOverlayView(scaffoldView)

                val animation = AnimationFactory.createStaticAnimation(
                    scaffoldView,
                    introTransitionSpec.animationType,
                    introTransitionSpec.animationDuration
                )
                viewIdentifierManager.addAnimation(scaffoldView.tag as String, animation!!)
                animation.start()

            }
        }

        private fun doAddViewWithDynamicAnimation(
            host: OverlayHost,
            scaffoldView: ScaffoldView,
            positionGuide: PositionGuide,
            introTransitionSpec: TransitionSpec,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(host)
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

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

            scaffoldView.layoutParams = layoutParams
            scaffoldView.visibility = View.INVISIBLE
            constraintSet.applyTo(host)
            host.addView(scaffoldView)
            viewIdentifierManager.attachOverlayView(scaffoldView)

            scaffoldView.doOnLayout {
                val x = scaffoldView.x
                val y = scaffoldView.y

                when (introTransitionSpec.animationType) {
                    SLIDE_FROM_LEFT -> {
                        scaffoldView.x =
                            -scaffoldView.width.toFloat()
                        val animation = ObjectAnimator.ofFloat(
                            scaffoldView,
                            View.X,
                            scaffoldView.x,
                            x
                        )
                        animation.duration = introTransitionSpec.animationDuration
                        animation.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                                scaffoldView.visibility = View.VISIBLE
                            }
                        })

                        animation.start()
                    }
                    SLIDE_FROM_RIGHT -> {
                        scaffoldView.x = host.width.toFloat()
                        val animation = ObjectAnimator.ofFloat(
                            scaffoldView,
                            View.X,
                            scaffoldView.x,
                            x
                        )
                        animation.duration = introTransitionSpec.animationDuration
                        animation.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationStart(animation: Animator?) {
                                scaffoldView.visibility = View.VISIBLE
                            }

                        })
                        viewIdentifierManager.addAnimation(
                            scaffoldView.tag as String,
                            animation
                        )
                        animation.start()
                    }
                    else -> {
                        // should not happen
                    }
                }

            }

        }

        fun removalViewWithAnimation(
            context: Context,
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            when (overlayEntity.outroTransitionSpec.animationType) {
                FADE_OUT -> {
                    removalViewWithStaticAnimation(
                        overlayHost,
                        overlayEntity,
                        viewIdentifierManager
                    )
                }
                SLIDE_TO_LEFT,
                SLIDE_TO_RIGHT -> {
                    removalViewWithDynamicAnimation(
                        overlayHost,
                        overlayEntity,
                        viewIdentifierManager
                    )
                }
                else -> {
                    // should not happen
                    return
                }

            }

        }

        private fun removalViewWithStaticAnimation(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {
                overlayHost.children.filter { it.tag == overlayEntity.id }.forEach { view ->
                    view as ScaffoldView
                    val animation = ObjectAnimator.ofFloat(view, View.ALPHA, 1F, 0F)
                    animation.duration = overlayEntity.outroTransitionSpec.animationDuration

                    animation.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            overlayHost.removeView(view)
                            viewIdentifierManager.detachOverlayView(view)
                            viewIdentifierManager.removeAnimation(overlayEntity.id)
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                    })

                    viewIdentifierManager.addAnimation(overlayEntity.id, animation)
                    animation.start()
                }
            }
        }

        private fun removalViewWithDynamicAnimation(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {
                overlayHost.children.filter { it.tag == overlayEntity.id }.forEach { view ->
                    view as ScaffoldView
                    var animation: ObjectAnimator? = null

                    if (overlayEntity.outroTransitionSpec.animationType == SLIDE_TO_LEFT) {
                        animation = ObjectAnimator.ofFloat(
                            view,
                            View.X,
                            view.x,
                            -view.width.toFloat()
                        )
                    } else if (overlayEntity.outroTransitionSpec.animationType == SLIDE_TO_RIGHT) {
                        animation = ObjectAnimator.ofFloat(
                            view,
                            View.X,
                            view.x,
                            overlayHost.width.toFloat()
                        )

                    }

                    if (animation == null) {
                        // should not happen
                        return@forEach
                    }

                    animation.duration = overlayEntity.outroTransitionSpec.animationDuration
                    animation.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            overlayHost.removeView(view)
                            viewIdentifierManager.detachOverlayView(view)
                            viewIdentifierManager.removeAnimation(overlayEntity.id)
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    })
                    viewIdentifierManager.addAnimation(overlayEntity.id, animation)
                    animation.start()
                }
            }
        }

        fun addLingeringIntroOverlay(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {

                val scaffoldView =
                    OverlayFactory.createScaffoldView(
                        overlayHost.context,
                        overlayEntity,
                        viewIdentifierManager.variableTranslator,
                        viewIdentifierManager.timeKeeper
                    )

                scaffoldView.doOnLayout {
                    val x = scaffoldView.x
                    val y = scaffoldView.y

                    when (overlayEntity.introTransitionSpec.animationType) {
                        FADE_IN -> {
                            scaffoldView.x =
                                -scaffoldView.width.toFloat()
                            val animation = ObjectAnimator.ofFloat(
                                scaffoldView,
                                View.ALPHA,
                                0F,
                                1F
                            )
                            animation.duration =
                                overlayEntity.introTransitionSpec.animationDuration
                            animation.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                                }

                                override fun onAnimationCancel(animation: Animator?) {
                                }

                                override fun onAnimationStart(animation: Animator?) {
                                    scaffoldView.visibility = View.VISIBLE
                                }

                            })
                            viewIdentifierManager.addAnimation(
                                overlayEntity.id,
                                animation
                            )
                            animation.start()
                            animation.currentPlayTime = animationPosition
                            if (isPlaying) {
                                animation.resume()
                            } else {
                                animation.pause()
                            }
                        }
                        SLIDE_FROM_LEFT -> {
                            scaffoldView.x =
                                -scaffoldView.width.toFloat()
                            val animation = ObjectAnimator.ofFloat(
                                scaffoldView,
                                View.X,
                                scaffoldView.x,
                                x
                            )
                            animation.duration =
                                overlayEntity.introTransitionSpec.animationDuration
                            animation.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                                }

                                override fun onAnimationCancel(animation: Animator?) {
                                }

                                override fun onAnimationStart(animation: Animator?) {
                                    scaffoldView.visibility = View.VISIBLE
                                }

                            })

                            viewIdentifierManager.addAnimation(
                                overlayEntity.id,
                                animation
                            )
                            animation.start()
                            animation.currentPlayTime = animationPosition
                            if (isPlaying) {
                                animation.resume()
                            } else {
                                animation.pause()
                            }
                        }
                        SLIDE_FROM_RIGHT -> {
                            scaffoldView.x = overlayHost.width.toFloat()
                            val animation = ObjectAnimator.ofFloat(
                                scaffoldView,
                                View.X,
                                scaffoldView.x,
                                x
                            )
                            animation.duration =
                                overlayEntity.introTransitionSpec.animationDuration
                            animation.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {
                                    scaffoldView.visibility = View.VISIBLE

                                }

                            })
                            viewIdentifierManager.addAnimation(
                                overlayEntity.id,
                                animation
                            )
                            animation.start()
                            animation.currentPlayTime = animationPosition
                            if (isPlaying) {
                                animation.resume()
                            } else {
                                animation.pause()
                            }
                        }
                        else -> {
                            // should not happen
                        }
                    }

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


                scaffoldView.layoutParams = layoutParams
                scaffoldView.visibility = View.INVISIBLE
                constraintSet.applyTo(overlayHost)
                overlayHost.addView(scaffoldView)
                viewIdentifierManager.attachOverlayView(scaffoldView)

            }

        }

        fun updateLingeringIntroOverlay(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {
                overlayHost.children.firstOrNull { it.tag == overlayEntity.id }?.let { view ->


                    viewIdentifierManager.getAnimationWithTag(overlayEntity.id)?.let {
                        it.cancel()
                    }
                    viewIdentifierManager.removeAnimation(overlayEntity.id)

                    val scaffoldView = viewIdentifierManager.getOverlayView(
                        overlayEntity.id
                    )
                    scaffoldView?.let {
                        viewIdentifierManager.detachOverlayView(
                            it
                        )
                    }


                    addLingeringIntroOverlay(
                        overlayHost,
                        overlayEntity,
                        animationPosition,
                        isPlaying,
                        viewIdentifierManager
                    )
                }
            }
        }

        fun updateLingeringMidwayOverlay(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            val scaffoldView = viewIdentifierManager.getOverlayView(overlayEntity.id) ?: return

            overlayHost.post {
                val constraintSet = ConstraintSet()
                constraintSet.clone(overlayHost)
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                val positionGuide = overlayEntity.viewSpec.positionGuide!!

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

                scaffoldView.layoutParams = layoutParams


                scaffoldView.visibility = View.VISIBLE
                constraintSet.applyTo(overlayHost)
            }

        }

        fun addLingeringOutroOverlay(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {

                val scaffoldView =
                    OverlayFactory.createScaffoldView(
                        overlayHost.context,
                        overlayEntity,
                        viewIdentifierManager.variableTranslator,
                        viewIdentifierManager.timeKeeper
                    )

                scaffoldView.doOnLayout {
                    when (overlayEntity.outroTransitionSpec.animationType) {
                        FADE_OUT -> {
                            val animation = ObjectAnimator.ofFloat(
                                scaffoldView,
                                View.ALPHA,
                                1F,
                                0F
                            )
                            animation.duration =
                                overlayEntity.outroTransitionSpec.animationDuration
                            animation.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                                    viewIdentifierManager.detachOverlayView(
                                        it as ScaffoldView
                                    )
                                    overlayHost.removeView(scaffoldView)
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {
                                    scaffoldView.visibility = View.VISIBLE
                                }

                            })

                            viewIdentifierManager.addAnimation(
                                overlayEntity.id,
                                animation
                            )
                            animation.start()
                            animation.currentPlayTime = animationPosition
                            if (isPlaying) {
                                animation.resume()
                            } else {
                                animation.pause()
                            }

                        }
                        SLIDE_TO_LEFT -> {

                            val animation = ObjectAnimator.ofFloat(
                                scaffoldView,
                                View.X,
                                scaffoldView.x,
                                -scaffoldView.width.toFloat()
                            )
                            animation.duration =
                                overlayEntity.outroTransitionSpec.animationDuration
                            animation.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                                    viewIdentifierManager.detachOverlayView(
                                        it as ScaffoldView
                                    )
                                    overlayHost.removeView(scaffoldView)
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {
                                    scaffoldView.visibility = View.VISIBLE
                                }

                            })

                            viewIdentifierManager.addAnimation(
                                overlayEntity.id,
                                animation
                            )
                            animation.start()
                            animation.currentPlayTime = animationPosition
                            if (isPlaying) {
                                animation.resume()
                            } else {
                                animation.pause()
                            }
                        }
                        SLIDE_TO_RIGHT -> {
                            val animation = ObjectAnimator.ofFloat(
                                scaffoldView,
                                View.X,
                                scaffoldView.x,
                                overlayHost.width.toFloat()
                            )
                            animation.duration =
                                overlayEntity.outroTransitionSpec.animationDuration
                            animation.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {
                                }

                                override fun onAnimationEnd(animation: Animator?) {
                                    viewIdentifierManager.removeAnimation(overlayEntity.id)
                                    viewIdentifierManager.detachOverlayView(
                                        it as ScaffoldView
                                    )
                                    overlayHost.removeView(scaffoldView)
                                }

                                override fun onAnimationCancel(animation: Animator?) {

                                }

                                override fun onAnimationStart(animation: Animator?) {
                                    scaffoldView.visibility = View.VISIBLE
                                }

                            })

                            viewIdentifierManager.addAnimation(
                                overlayEntity.id,
                                animation
                            )
                            animation.start()
                            animation.currentPlayTime = animationPosition
                            if (isPlaying) {
                                animation.resume()
                            } else {
                                animation.pause()
                            }
                        }
                        else -> {
                            // should not happen
                        }
                    }

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

                scaffoldView.layoutParams = layoutParams
                scaffoldView.visibility = View.INVISIBLE
                constraintSet.applyTo(overlayHost)
                overlayHost.addView(scaffoldView)
                viewIdentifierManager.attachOverlayView(scaffoldView)

            }

        }

        fun updateLingeringOutroOverlay(
            overlayHost: OverlayHost,
            overlayEntity: OverlayEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            // TODO: 27/07/2020
            val scaffoldView = viewIdentifierManager.getOverlayView(overlayEntity.id) ?: return

            overlayHost.post {

                viewIdentifierManager.getAnimationWithTag(overlayEntity.id)?.let {
                    it.cancel()
                }
                viewIdentifierManager.removeAnimation(overlayEntity.id)

                viewIdentifierManager.detachOverlayView(
                    viewIdentifierManager.getOverlayView(
                        overlayEntity.id
                    )!!
                )

                addLingeringOutroOverlay(
                    overlayHost,
                    overlayEntity,
                    animationPosition,
                    isPlaying,
                    viewIdentifierManager
                )
            }

        }


        /**endregion */


    }
}
