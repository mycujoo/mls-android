package tv.mycujoo.mls.manager.contracts

import android.animation.ObjectAnimator
import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.mls.widgets.ScaffoldView

interface IViewHandler {

    fun setOverlayHost(overlayHost: ConstraintLayout)
    fun getOverlayHost(): ConstraintLayout


    fun addAnimation(id: String, objectAnimator: ObjectAnimator)
    fun removeAnimation(id: String)
    fun getAnimations(): List<ObjectAnimator>
    fun getAnimationWithTag(id: String): ObjectAnimator?

    fun attachOverlayView(view: ScaffoldView)
    fun detachOverlayView(view: ScaffoldView?)
    fun getOverlayView(id: String): ScaffoldView?

    fun overlayIsNotAttached(id: String): Boolean
    fun overlayIsAttached(id: String): Boolean

    fun clearAll()
    fun incrementIdlingResource()
    fun decrementIdlingResource()
}
