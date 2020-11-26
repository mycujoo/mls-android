package tv.mycujoo.mls.manager.contracts

import android.animation.ObjectAnimator
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

interface IViewHandler {

    fun setOverlayHost(overlayHost: OverlayHost)
    fun getOverlayHost(): OverlayHost


    fun addAnimation(id: String, objectAnimator: ObjectAnimator)
    fun removeAnimation(id: String)
    fun getAnimations(): List<ObjectAnimator>
    fun getAnimationWithTag(id: String): ObjectAnimator?

    fun attachOverlayView(view: ScaffoldView)
    fun detachOverlayView(view: ScaffoldView?)
    fun getOverlayView(id: String): ScaffoldView?

    fun overlayBlueprintIsNotAttached(id: String): Boolean
    fun overlayBlueprintIsAttached(id: String): Boolean

    fun clearAll()
    fun incrementIdlingResource()
    fun decrementIdlingResource()

    fun getVariableTranslator(): VariableTranslator
    fun getVariableKeeper(): IVariableKeeper
}
