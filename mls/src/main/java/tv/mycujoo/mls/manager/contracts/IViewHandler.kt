package tv.mycujoo.mls.manager.contracts

import android.animation.ObjectAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.mls.widgets.ScaffoldView

interface IViewHandler {

    fun setOverlayHost(overlayHost: ConstraintLayout)
    fun getOverlayHost(): ConstraintLayout


    fun addAnimation(overlayTag: String, objectAnimator: ObjectAnimator)
    fun removeAnimation(overlayTag: String)
    fun getAnimations(): List<ObjectAnimator>
    fun getAnimationWithTag(id: String): ObjectAnimator?

    fun attachOverlayView(view: ScaffoldView)
    fun detachOverlayView(view: ScaffoldView?)
    fun getOverlayView(id: String): ScaffoldView?

    fun overlayIsNotAttached(tag: String): Boolean
    fun overlayIsAttached(tag: String): Boolean

    fun clearAll()
    fun incrementIdlingResource()
    fun decrementIdlingResource()
}
