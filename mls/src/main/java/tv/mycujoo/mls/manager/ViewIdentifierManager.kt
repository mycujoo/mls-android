package tv.mycujoo.mls.manager

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.mls.widgets.ScaffoldView

class ViewIdentifierManager {
    private var viewIdToIdMap = mutableMapOf<String, Int>()
    private var viewIdToAnimationMap = mutableMapOf<Int, ObjectAnimator>()

    private var animations = ArrayList<ObjectAnimator>()

    val attachedAnimationIdList: ArrayList<String> = ArrayList()

    val attachedViewList: ArrayList<View> = ArrayList()


    fun storeViewId(view: View, customId: String) {
        viewIdToIdMap[customId] = view.id
    }

    fun getViewId(customId: String): Int? {
        return viewIdToIdMap[customId]
    }


    fun addAnimation(objectAnimator: ObjectAnimator) {
        animations.add(objectAnimator)
    }

    fun getAnimations(): List<ObjectAnimator> {
        return animations
    }

    /**region Attached Overlay objects & ids*/
    fun attachOverlayView(view: View) {
        if (view.tag == null || (view.tag is String).not()) {
            Log.w("ViewIdentifierManager", "overlay tag should not be null")
            return
        }

        if (attachedViewList.any { it.tag == view.tag as String }) {
            Log.w("ViewIdentifierManager", "Should not add an already active view")
        } else {
            attachedViewList.add(view)
        }

    }

    fun detachOverlayView(view: View) {
        if (view.tag == null || (view.tag is String).not()) {
            Log.w("ViewIdentifierManager", "overlay tag should not be null [detachOverlay()]")
            return
        }

        attachedViewList.remove(view)
    }

    /**endregion */


    fun attachAnimation(id: String) {
        attachedAnimationIdList.add(id)
    }

    fun detachAnimationWithTag(id: String) {
        attachedAnimationIdList.remove(id)
    }

    fun overlayObjectIsNotAttached(id: String): Boolean {
        return attachedViewList.none { it.tag == id }
    }

    fun overlayObjectIsAttached(id: String): Boolean {
        return attachedViewList.any { it.tag == id }
    }

    fun applySetVariable(variable: Variable) {
        attachedViewList.filterIsInstance<ScaffoldView>().forEach {
            it.setVariable(variable)
        }
    }

    fun clearAll() {
        attachedViewList.clear()
        attachedAnimationIdList.clear()
        animations.clear()
        viewIdToIdMap.clear()
    }

}