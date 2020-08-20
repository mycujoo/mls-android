package tv.mycujoo.mls.manager

import android.animation.ObjectAnimator
import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.OverlayHost
import tv.mycujoo.mls.widgets.ScaffoldView

open class ViewHandler(
    dispatcher: CoroutineScope,
    val idlingResource: CountingIdlingResource
) : IViewHandler {


    /**region Fields*/
    private lateinit var overlayHost: OverlayHost

    private var viewIdToIdMap = mutableMapOf<String, Int>()
    private var animations = ArrayList<Pair<String, ObjectAnimator>>()

    private val attachedViewList: ArrayList<ScaffoldView> = ArrayList()

    private val variableTranslator = VariableTranslator(dispatcher)
    private val timeKeeper = TimeKeeper(dispatcher)

    /**endregion */

    /**region OverlayHost*/
    override fun setOverlayHost(overlayHost: OverlayHost) {
        this.overlayHost = overlayHost
    }

    override fun getOverlayHost(): OverlayHost {
       return overlayHost
    }
    /**endregion */

    /**region Animation*/
    override fun addAnimation(
        id: String,
        objectAnimator: ObjectAnimator
    ) {
        animations.add(Pair(id, objectAnimator))
    }

    override fun removeAnimation(id: String) {
        val pair = animations.firstOrNull { it.first == id }
        animations.remove(pair)
    }

    override fun getAnimations(): List<ObjectAnimator> {
        return animations.map { it.second }
    }

    override fun getAnimationWithTag(id: String): ObjectAnimator? {
        return animations.firstOrNull { it.first == id }?.second
    }
    /**endregion */

    /**region Overlay views*/
    override fun attachOverlayView(view: ScaffoldView) {
        if (view.tag == null || (view.tag is String).not()) {
            Log.w("ViewIdentifierManager", "overlay tag should not be null")
            return
        }
        val viewTag = view.tag as String
        if (attachedViewList.any {
                it.tag == viewTag }) {
            Log.w("ViewIdentifierManager", "Should not add an already active view")
        } else {
            attachedViewList.add(view)
            overlayHost.addView(view)
        }
    }

    override fun detachOverlayView(view: ScaffoldView?) {
        if (view == null) {
            return
        }

        if (view.tag == null || (view.tag is String).not()) {
            Log.w("ViewIdentifierManager", "overlay tag should not be null [detachOverlay()]")
            return
        }

        attachedViewList.remove(view)
        overlayHost.removeView(view)
    }

    override fun getOverlayView(id: String): ScaffoldView? {
        return attachedViewList.firstOrNull { it.tag == id }
    }

    /**endregion */

    /**region Overlay objects*/
    override fun overlayBlueprintIsNotAttached(id: String): Boolean {
        return attachedViewList.none { it.tag == id }
    }

    override fun overlayBlueprintIsAttached(id: String): Boolean {
        return attachedViewList.any { it.tag == id }
    }

    /**endregion */

    /**region msc*/
    override fun clearAll() {
        attachedViewList.clear()
        animations.clear()
        viewIdToIdMap.clear()
    }

    override fun incrementIdlingResource() {
        idlingResource.increment()
    }


    override fun decrementIdlingResource() {
        if (idlingResource.isIdleNow.not()) {
            idlingResource.decrement()
        }
    }

    override fun getVariableTranslator(): VariableTranslator {
        return variableTranslator
    }

    override fun getTimeKeeper(): TimeKeeper {
        return timeKeeper
    }
    /**endregion */


}