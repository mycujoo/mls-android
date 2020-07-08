package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.children
import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ProportionalImageView

class OverlayViewHelper {
    companion object {

        /**region Add View*/
        fun addView(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            overlayEntity: ShowOverlayActionEntity,
            objectAnimator: ObjectAnimator?,
            viewIdentifierManager: ViewIdentifierManager,
            idlingResource: CountingIdlingResource
        ) {

            if (hasNoAnimation(overlayEntity)) {
                addViewWithNoAnimation(
                    host,
                    proportionalImageView,
                    positionGuide,
                    overlayEntity,
                    idlingResource
                )
                return
            }

            if (hasDynamicIntroAnimation(overlayEntity.animationType)) {
                addViewWithDynamicAnimation(
                    host,
                    proportionalImageView,
                    positionGuide,
                    overlayEntity,
                    viewIdentifierManager,
                    idlingResource
                )
            } else if (hasStaticIntroAnimation(overlayEntity.animationType)) {
                addViewWithStaticAnimation(
                    host,
                    proportionalImageView,
                    positionGuide,
                    overlayEntity,
                    objectAnimator,
                    viewIdentifierManager,
                    idlingResource
                )
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
            viewIdentifierManager: ViewIdentifierManager,
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
                objectAnimator?.let {
                    viewIdentifierManager.storeAnimation(proportionalImageView.id, it)
                }
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
            viewIdentifierManager: ViewIdentifierManager,
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
                                        animation.duration = overlayEntity.animationDuration
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

                                        viewIdentifierManager.storeAnimation(
                                            proportionalImageView.id,
                                            animation
                                        )
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
                                        animation.duration = overlayEntity.animationDuration
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
                                        viewIdentifierManager.storeAnimation(
                                            proportionalImageView.id,
                                            animation
                                        )
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

        fun addViewWithLingeringIntroAnimation(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            overlayEntity: ShowOverlayActionEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager,
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
                                        animation.duration = overlayEntity.animationDuration
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

                                        viewIdentifierManager.storeAnimation(
                                            proportionalImageView.id,
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
                                    SLIDE_FROM_TRAILING -> {
                                        proportionalImageView.x = host.width.toFloat()
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            x
                                        )
                                        animation.duration = overlayEntity.animationDuration
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
                                        viewIdentifierManager.storeAnimation(
                                            proportionalImageView.id,
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

        fun addViewWithLingeringOutroAnimation(
            host: OverlayHost,
            proportionalImageView: ProportionalImageView,
            positionGuide: PositionGuide,
            relatedShowOverlayEntity: ShowOverlayActionEntity,
            hideOverlayEntity: HideOverlayActionEntity,
            animationPosition: Long,
            isPlaying: Boolean,
            viewIdentifierManager: ViewIdentifierManager,
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
                                when (hideOverlayEntity.animationType) {
                                    NONE,
                                    FADE_OUT,
                                    FADE_IN -> {
                                        // should not happen
                                    }
                                    SLIDE_TO_LEADING -> {

                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            -proportionalImageView.width.toFloat()
                                        )
                                        animation.duration = hideOverlayEntity.animationDuration
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

                                        viewIdentifierManager.storeAnimation(
                                            proportionalImageView.id,
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
                                    SLIDE_TO_TRAILING -> {
                                        val animation = ObjectAnimator.ofFloat(
                                            proportionalImageView,
                                            View.X,
                                            proportionalImageView.x,
                                            proportionalImageView.width.toFloat()
                                        )
                                        animation.duration = hideOverlayEntity.animationDuration
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
                                        viewIdentifierManager.storeAnimation(
                                            proportionalImageView.id,
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

        /**endregion */

        /**region Animating Outro in normal playback*/
        fun addOutroAnimationToCurrentOverlay(
            host: OverlayHost,
            customId: String?,
            hideOverlayEntity: ActionEntity,
            viewIdentifierManager: ViewIdentifierManager
        ) {
            if (customId.isNullOrEmpty() || hideOverlayEntity.animationDuration == null) {
                return
            }

            val viewId = viewIdentifierManager.getViewId(customId) ?: return
            host.children.first { it.id == viewId }.let { proportionalImageView ->
                host.post {
                    when (hideOverlayEntity.animationType) {
                        NONE,
                        FADE_OUT,
                        FADE_IN -> {
                            // should not happen
                        }
                        SLIDE_TO_LEADING -> {

                            val animation = ObjectAnimator.ofFloat(
                                proportionalImageView,
                                View.X,
                                proportionalImageView.x,
                                -proportionalImageView.width.toFloat()
                            )
                            animation.duration = hideOverlayEntity.animationDuration
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

                            viewIdentifierManager.storeAnimation(
                                proportionalImageView.id,
                                animation
                            )
                            animation.start()
                        }
                        SLIDE_TO_TRAILING -> {
                            val animation = ObjectAnimator.ofFloat(
                                proportionalImageView,
                                View.X,
                                proportionalImageView.x,
                                proportionalImageView.width.toFloat()
                            )
                            animation.duration = hideOverlayEntity.animationDuration
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
                            viewIdentifierManager.storeAnimation(
                                proportionalImageView.id,
                                animation
                            )
                            animation.start()
                        }
                    }

                }
            }

        }

        /**endregion */


        /**region Remove View*/
        fun removeView(
            host: OverlayHost,
            viewId: Int?,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {

            if (viewId == null) {
                return
            }

            if (overlayEntity.animationType == NONE || overlayEntity.animationDuration <= 0L) {
                removeViewWithNoAnimation(host, viewId, overlayEntity, idlingResource)
                return
            }


            if (hasDynamicOutroAnimation(overlayEntity.animationType)) {
                removeViewWithDynamicAnimation(host, viewId, overlayEntity, idlingResource)
            } else if (hasStaticOutroAnimation(overlayEntity.animationType)) {
                removeViewWithStaticAnimation(host, viewId, overlayEntity, idlingResource)
            }

        }

        private fun removeViewWithNoAnimation(
            host: OverlayHost,
            viewId: Int,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {
            host.post(Runnable {
                host.children.forEach { view ->
                    if (view.id == viewId) {

                        (host as ViewGroup).setOnHierarchyChangeListener(object :
                            ViewGroup.OnHierarchyChangeListener {
                            override fun onChildViewRemoved(parent: View?, child: View?) {
                                if (child != null && child.id == viewId) {
                                    if (!idlingResource.isIdleNow) {
                                        idlingResource.decrement()
                                    }
                                }

                            }

                            override fun onChildViewAdded(parent: View?, child: View?) {}
                        })

                        host.removeView(view)

                        return@Runnable
                    }
                }
            })

        }


        private fun removeViewWithDynamicAnimation(
            host: OverlayHost,
            viewId: Int,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {

        }

        private fun removeViewWithStaticAnimation(
            host: OverlayHost,
            viewId: Int,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {

            host.post(Runnable {
                host.children.forEach { view ->
                    if (view.id == viewId) {

                        val animation = ObjectAnimator.ofFloat(view, View.ALPHA, 1F, 0F)
                        animation.duration = overlayEntity.animationDuration

                        animation.addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                host.removeView(view)
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                        })

                        animation.start()


                        if (!idlingResource.isIdleNow) {
                            idlingResource.decrement()
                        }
                        return@Runnable
                    }
                }
            })


        }

        fun clearScreen(
            host: OverlayHost,
            viewIdentifierToBeCleared: List<Int?>,
            idlingResource: CountingIdlingResource
        ) {

            host.post(Runnable {
                host.children.forEach { view ->
                    if (viewIdentifierToBeCleared.contains(view.id)) {
                        host.removeView(view)
                    }
                }

                if (!idlingResource.isIdleNow) {
                    idlingResource.decrement()
                }
                return@Runnable
            })


        }
        /**endregion */


        /**region Private Functions*/

        private fun hasNoAnimation(overlayEntity: HideOverlayActionEntity): Boolean {
            return overlayEntity.animationType == NONE
        }

        private fun hasNoAnimation(overlayEntity: ShowOverlayActionEntity): Boolean {
            return overlayEntity.animationType == NONE
        }

        private fun hasDynamicIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                NONE,
                FADE_IN,
                FADE_OUT,
                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
                    false
                }

                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING -> {
                    true
                }
            }
        }

        private fun hasStaticIntroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                FADE_IN -> {
                    true
                }

                NONE,
                FADE_OUT,
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING,
                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
                    false
                }
            }
        }

        private fun hasDynamicOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                NONE,
                FADE_IN,
                FADE_OUT,
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING -> {
                    false
                }

                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
                    true
                }
            }
        }

        private fun hasStaticOutroAnimation(animationType: AnimationType): Boolean {
            return when (animationType) {
                FADE_OUT -> {
                    true
                }

                NONE,
                FADE_IN,
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING,
                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
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

        /**endregion */
    }
}
