package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.widgets.OverlayHost

class OverlayRemoveHelper {
    companion object {

        fun executeInFuture(
            host: OverlayHost,
            commandAction: CommandAction,
            viewIdentifier: Int?,
            idlingResource: CountingIdlingResource
        ) {
            host.postDelayed({
                host.children.forEach { view ->
                    if (view.id == viewIdentifier) {
                        when {
                            commandAction.verb.equals("remove", true) -> {
                                host.removeView(view)
                            }
                            commandAction.verb.equals("hide", true) -> {
                                view.visibility = View.INVISIBLE
                            }
                            commandAction.verb.equals("show", true) -> {
                                view.visibility = View.VISIBLE
                            }
                        }

                        if (!idlingResource.isIdleNow) {
                            idlingResource.decrement()
                        }
                    }
                }
            }, commandAction.offset)
        }

        fun isRemoveOrHide(commandAction: CommandAction): Boolean {
            return when {
                commandAction.verb.equals("remove", true) -> {
                    true
                }
                commandAction.verb.equals("hide", true) -> {
                    true
                }
                else -> {
                    false
                }
            }
        }

        private fun hasDynamicAnimation(overlayEntity: HideOverlayActionEntity): Boolean {
            return when (overlayEntity.animationType) {
                NONE,
                FADE_IN,
                FADE_OUT -> {
                    false
                }
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING,
                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
                    true
                }
            }
        }

        private fun hasStaticAnimation(overlayEntity: HideOverlayActionEntity): Boolean {
            return when (overlayEntity.animationType) {
                NONE -> {
                    false
                }
                FADE_IN,
                FADE_OUT -> {
                    true
                }
                SLIDE_FROM_LEADING,
                SLIDE_FROM_TRAILING,
                SLIDE_TO_LEADING,
                SLIDE_TO_TRAILING -> {
                    false
                }
            }
        }


        fun removeView(
            host: OverlayHost,
            viewId: Int?,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {

            if (overlayEntity.animationType == NONE || overlayEntity.animationDuration <= 0L) {
                removeViewWithNoAnimation(host, viewId, overlayEntity, idlingResource)
                return
            }


            if (hasDynamicAnimation(overlayEntity)) {
                removeViewWithDynamicAnimation(host, viewId, overlayEntity, idlingResource)
            } else if (hasStaticAnimation(overlayEntity)) {
                removeViewWithStaticAnimation(host, viewId, overlayEntity, idlingResource)
            }

        }

        private fun removeViewWithNoAnimation(
            host: OverlayHost,
            viewId: Int?,
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
            viewId: Int?,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {

        }

        private fun removeViewWithStaticAnimation(
            host: OverlayHost,
            viewId: Int?,
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

    }
}