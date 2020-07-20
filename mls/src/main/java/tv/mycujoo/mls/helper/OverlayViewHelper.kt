package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayViewHelper {
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


        // re-write
        fun addViewWithNoAnimation(
            context: Context,
            overlayHost: OverlayHost,
            overlayObject: OverlayObject,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            val scaffoldView =
                OverlayFactory.createScaffold(
                    context,
                    overlayObject.id,
                    overlayObject.viewSpec.size!!
                )

            try {
                var svg: SVG
                overlayObject.svgData!!.svgString!!.let {
                    svg = SVG.getFromString(it)
                }
                svg.setDocumentWidth("100%")
                svg.setDocumentHeight("100%")
                scaffoldView.setSVG(svg)
                scaffoldView.setSVGSource(overlayObject.svgData!!.svgString!!)

                scaffoldView.setVariablePlaceHolder(overlayObject.variablePlaceHolders)

                doAddViewWithNoAnimation(
                    overlayHost,
                    scaffoldView,
                    overlayObject.viewSpec.positionGuide!!,
                    viewIdentifierManager
                )

            } catch (e: Exception) {
                Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
            }

        }

        fun addViewWithAnimation(
            context: Context,
            overlayHost: OverlayHost,
            overlayObject: OverlayObject,
            viewIdentifierManager: ViewIdentifierManager
        ) {

            overlayHost.post {
                val proportionalImageView =
                    OverlayFactory.create(context, overlayObject.id, overlayObject.viewSpec.size!!)

                try {
                    var svg: SVG
                    overlayObject.svgData!!.svgString!!.let {
                        svg = SVG.getFromString(it)
                    }
                    svg.setDocumentWidth("100%")
                    svg.setDocumentHeight("100%")
                    proportionalImageView.setSVG(svg)

                    when (overlayObject.introTransitionSpec.animationType) {
                        FADE_IN -> {
                            doAddViewWithStaticAnimation(
                                overlayHost,
                                proportionalImageView,
                                overlayObject.viewSpec.positionGuide!!,
                                overlayObject.introTransitionSpec,
                                viewIdentifierManager
                            )
                        }
                        SLIDE_FROM_LEFT,
                        SLIDE_FROM_RIGHT -> {

                            doAddViewWithDynamicAnimation(
                                overlayHost,
                                proportionalImageView,
                                overlayObject.viewSpec.positionGuide!!,
                                overlayObject.introTransitionSpec,
                                viewIdentifierManager
                            )
                        }
                        else -> {
                            // should not happen
                        }
                    }

                } catch (e: Exception) {
                    Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
                }

            }
        }

        private fun doAddViewWithStaticAnimation(
            overlayHost: OverlayHost,
            proportionalImageView: ProportionalImageView,
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
                    setLeftConstraints(constraintSet, it, layoutParams, proportionalImageView)
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
                constraintSet.applyTo(overlayHost)
                proportionalImageView.layoutParams = layoutParams

                overlayHost.addView(proportionalImageView)
                viewIdentifierManager.attachOverlayView(proportionalImageView)

                val animation = AnimationFactory.createStaticAnimation(
                    proportionalImageView,
                    introTransitionSpec.animationType,
                    introTransitionSpec.animationDuration
                )
                viewIdentifierManager.addAnimation(animation!!)
                animation.start()

            }
        }

        private fun doAddViewWithDynamicAnimation(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            introTransitionSpec: TransitionSpec,
            viewIdentifierManager: ViewIdentifierManager
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

                                when (introTransitionSpec.animationType) {
                                    SLIDE_FROM_LEFT -> {
                                        proportionalImageView.x =
                                            -proportionalImageView.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
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
                                                proportionalImageView.visibility = View.VISIBLE
                                            }
                                        })

                                        animation.start()
                                    }
                                    SLIDE_FROM_RIGHT -> {
                                        proportionalImageView.x = host.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
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
                                                proportionalImageView.visibility = View.VISIBLE

                                            }

                                        })
                                        viewIdentifierManager.addAnimation(animation)
                                        animation.start()
                                    }
                                    else -> {
                                        // should not happen
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

                positionGuide.left?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setLeftConstraints(constraintSet, it, layoutParams, proportionalImageView)
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
                viewIdentifierManager.attachOverlayView(proportionalImageView)

            }

        }


        private fun doAddViewWithNoAnimation(
            overlayHost: OverlayHost,
            proportionalImageView: View,
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
                    setLeftConstraints(constraintSet, it, layoutParams, proportionalImageView)
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

                constraintSet.applyTo(overlayHost)
                proportionalImageView.layoutParams = layoutParams

                overlayHost.addView(proportionalImageView)
                viewIdentifierManager.attachOverlayView(proportionalImageView)
            }
        }

        // remove view [checks animation type and calls related functions]
        fun removalViewWithAnimation(
            context: Context,
            overlayHost: OverlayHost,
            overlayObject: OverlayObject,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            when (overlayObject.outroTransitionSpec.animationType) {
                FADE_OUT -> {
                    removalViewWithStaticAnimation(
                        overlayHost,
                        overlayObject,
                        viewIdentifierManager
                    )
                }
                SLIDE_TO_LEFT,
                SLIDE_TO_RIGHT -> {
                    removalViewWithDynamicAnimation(
                        overlayHost,
                        overlayObject,
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
            overlayObject: OverlayObject,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {
                overlayHost.children.filter { it.tag == overlayObject.id }.forEach { view ->
                    val animation = ObjectAnimator.ofFloat(view, View.ALPHA, 1F, 0F)
                    animation.duration = overlayObject.outroTransitionSpec.animationDuration

                    animation.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            overlayHost.removeView(view)
                            viewIdentifierManager.detachOverlayView(view)
                            viewIdentifierManager.detachAnimationWithTag(overlayObject.id)
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                    })

                    viewIdentifierManager.addAnimation(animation)
                    viewIdentifierManager.attachedAnimationIdList.add(overlayObject.id)
                    animation.start()
                }
            }
        }

        private fun removalViewWithDynamicAnimation(
            overlayHost: OverlayHost,
            overlayObject: OverlayObject,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {
                overlayHost.children.filter { it.tag == overlayObject.id }.forEach { view ->
                    var animation: ObjectAnimator? = null

                    if (overlayObject.outroTransitionSpec.animationType == SLIDE_TO_LEFT) {
                        animation = ObjectAnimator.ofFloat(
                            view,
                            View.X,
                            view.x,
                            -view.width.toFloat()
                        )
                    } else if (overlayObject.outroTransitionSpec.animationType == SLIDE_TO_RIGHT) {
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

                    animation.duration = overlayObject.outroTransitionSpec.animationDuration
                    animation.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            overlayHost.removeView(view)
                            viewIdentifierManager.detachAnimationWithTag(overlayObject.id)
                            viewIdentifierManager.detachOverlayView(view)

                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                    })
                    viewIdentifierManager.addAnimation(animation)
                    viewIdentifierManager.attachAnimation(overlayObject.id)
                    animation.start()
                }
            }
        }

        // lingerings
        fun onLingeringIntroAnimationOverlay(
            overlayHost: OverlayHost,
            overlayObject: OverlayObject,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {

                val proportionalImageView =
                    OverlayFactory.create(
                        overlayHost.context,
                        overlayObject.id,
                        overlayObject.viewSpec.size!!
                    )

                try {
                    var svg: SVG
                    overlayObject.svgData!!.svgString!!.let {
                        svg = SVG.getFromString(it)
                    }
                    svg.setDocumentWidth("100%")
                    svg.setDocumentHeight("100%")
                    proportionalImageView.setSVG(svg)
                } catch (e: Exception) {
                    Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
                }


                (overlayHost as ViewGroup).setOnHierarchyChangeListener(object :
                    ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewRemoved(parent: View?, child: View?) {

                    }

                    override fun onChildViewAdded(parent: View?, child: View?) {
                        if (child != null && child.tag == proportionalImageView.tag) {
                            overlayHost.post {


                                val x = proportionalImageView.x
                                val y = proportionalImageView.y

                                when (overlayObject.introTransitionSpec.animationType) {
                                    FADE_IN -> {
                                        proportionalImageView.x =
                                            -proportionalImageView.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.ALPHA,
                                            0F,
                                            1F
                                        )
                                        animation.duration =
                                            overlayObject.introTransitionSpec.animationDuration
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                viewIdentifierManager.attachedAnimationIdList.remove(
                                                    overlayObject.id
                                                )
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {
                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE
                                            }

                                        })

                                        viewIdentifierManager.addAnimation(animation)
                                        viewIdentifierManager.attachAnimation(
                                            overlayObject.id
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
                                        proportionalImageView.x =
                                            -proportionalImageView.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            x
                                        )
                                        animation.duration =
                                            overlayObject.introTransitionSpec.animationDuration
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                viewIdentifierManager.attachedAnimationIdList.remove(
                                                    overlayObject.id
                                                )
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {
                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE
                                            }

                                        })

                                        viewIdentifierManager.addAnimation(animation)
                                        viewIdentifierManager.attachedAnimationIdList.add(
                                            overlayObject.id
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
                                        proportionalImageView.x = overlayHost.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            x
                                        )
                                        animation.duration =
                                            overlayObject.introTransitionSpec.animationDuration
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                viewIdentifierManager.attachedAnimationIdList.remove(
                                                    overlayObject.id
                                                )
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {

                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE

                                            }

                                        })
                                        viewIdentifierManager.addAnimation(animation)
                                        viewIdentifierManager.attachedAnimationIdList.add(
                                            overlayObject.id
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
                        }

                    }

                })

                val constraintSet = ConstraintSet()
                constraintSet.clone(overlayHost)
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                val positionGuide = overlayObject.viewSpec.positionGuide
                if (positionGuide == null) {
                    // should not happen
                    return@post
                }
                positionGuide.left?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setLeftConstraints(constraintSet, it, layoutParams, proportionalImageView)
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
                constraintSet.applyTo(overlayHost)
                overlayHost.addView(proportionalImageView)
                viewIdentifierManager.attachOverlayView(proportionalImageView)

            }
        }

        fun onLingeringOutroAnimationOverlay(
            overlayHost: OverlayHost,
            overlayObject: OverlayObject,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            overlayHost.post {

                val proportionalImageView =
                    OverlayFactory.create(
                        overlayHost.context,
                        overlayObject.id,
                        overlayObject.viewSpec.size!!
                    )

                try {
                    var svg: SVG
                    overlayObject.svgData!!.svgString!!.let {
                        svg = SVG.getFromString(it)
                    }
                    svg.setDocumentWidth("100%")
                    svg.setDocumentHeight("100%")
                    proportionalImageView.setSVG(svg)
                } catch (e: Exception) {
                    Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
                }

                (overlayHost as ViewGroup).setOnHierarchyChangeListener(object :
                    ViewGroup.OnHierarchyChangeListener {
                    override fun onChildViewRemoved(parent: View?, child: View?) {
                    }

                    override fun onChildViewAdded(parent: View?, child: View?) {
                        if (child != null && child.tag == proportionalImageView.tag) {
                            overlayHost.post {
                                when (overlayObject.outroTransitionSpec.animationType) {
                                    FADE_OUT -> {
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.ALPHA,
                                            1F,
                                            0F
                                        )
                                        animation.duration =
                                            overlayObject.outroTransitionSpec.animationDuration
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                viewIdentifierManager.detachAnimationWithTag(
                                                    overlayObject.id
                                                )
                                                viewIdentifierManager.detachOverlayView(
                                                    child
                                                )
                                                overlayHost.removeView(proportionalImageView)
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {

                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE
                                            }

                                        })

                                        viewIdentifierManager.addAnimation(animation)
                                        viewIdentifierManager.attachAnimation(
                                            overlayObject.id
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
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            -proportionalImageView.width.toFloat()
                                        )
                                        animation.duration =
                                            overlayObject.outroTransitionSpec.animationDuration
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {
                                                viewIdentifierManager.detachAnimationWithTag(
                                                    overlayObject.id
                                                )
                                                viewIdentifierManager.detachOverlayView(
                                                    child
                                                )
                                                overlayHost.removeView(proportionalImageView)
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {

                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE
                                            }

                                        })

                                        viewIdentifierManager.addAnimation(animation)
                                        viewIdentifierManager.attachAnimation(
                                            overlayObject.id
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
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            overlayHost.width.toFloat()
                                        )
                                        animation.duration =
                                            overlayObject.outroTransitionSpec.animationDuration
                                        animation.addListener(object : Animator.AnimatorListener {
                                            override fun onAnimationRepeat(animation: Animator?) {
                                            }

                                            override fun onAnimationEnd(animation: Animator?) {

                                                viewIdentifierManager.detachAnimationWithTag(
                                                    overlayObject.id
                                                )
                                                viewIdentifierManager.detachOverlayView(
                                                    child
                                                )
                                                overlayHost.removeView(proportionalImageView)
                                            }

                                            override fun onAnimationCancel(animation: Animator?) {

                                            }

                                            override fun onAnimationStart(animation: Animator?) {
                                                proportionalImageView.visibility = View.VISIBLE
                                            }

                                        })

                                        viewIdentifierManager.addAnimation(animation)
                                        viewIdentifierManager.attachAnimation(
                                            overlayObject.id
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
                        }

                    }

                })

                val constraintSet = ConstraintSet()
                constraintSet.clone(overlayHost)
                val layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )
                val positionGuide = overlayObject.viewSpec.positionGuide
                if (positionGuide == null) {
                    // should not happen
                    return@post
                }
                positionGuide.left?.let {
                    if (it < 0F) {
                        return@let
                    }
                    setLeftConstraints(constraintSet, it, layoutParams, proportionalImageView)
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
                constraintSet.applyTo(overlayHost)
                overlayHost.addView(proportionalImageView)
                viewIdentifierManager.attachOverlayView(proportionalImageView)

            }
        }

    }
}
