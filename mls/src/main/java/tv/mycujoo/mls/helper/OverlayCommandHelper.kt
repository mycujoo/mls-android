package tv.mycujoo.mls.helper

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import androidx.core.view.children
import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.domain.entity.HideOverlayActionEntity
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.widgets.OverlayHost

class OverlayCommandHelper {
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


        fun removeView(
            host: OverlayHost,
            viewIdentifier: Int?,
            overlayEntity: HideOverlayActionEntity,
            idlingResource: CountingIdlingResource
        ) {
            host.post(Runnable {
                host.children.forEach { view ->
                    if (view.id == viewIdentifier) {

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

    }
}