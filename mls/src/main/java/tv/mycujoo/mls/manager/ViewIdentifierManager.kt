package tv.mycujoo.mls.manager

import android.animation.ObjectAnimator
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope

class ViewIdentifierManager(dispatcher: CoroutineScope) {
    private var viewIdToIdMap = mutableMapOf<String, Int>()
    private var animations = ArrayList<Pair<String, ObjectAnimator>>()

    private val attachedViewList: ArrayList<View> = ArrayList()

    val variableTranslator = VariableTranslator(dispatcher)
    val timeKeeper = TimeKeeper(dispatcher)


    fun storeViewId(view: View, customId: String) {
        viewIdToIdMap[customId] = view.id
    }

    fun getViewId(customId: String): Int? {
        return viewIdToIdMap[customId]
    }


    fun addAnimation(
        id: String,
        objectAnimator: ObjectAnimator
    ) {
        animations.add(Pair(id, objectAnimator))
    }

    fun removeAnimation(id: String) {
        val pair = animations.firstOrNull { it.first == id }
        animations.remove(pair)
    }

    fun getAnimations(): List<ObjectAnimator> {
        return animations.map { it.second }
    }

    fun hasNoActiveAnimation(id: String): Boolean {
        return animations.any { it.first == id }
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
    fun getAnimationWithTag(id: String): ObjectAnimator? {
        return animations.firstOrNull { it.first == id }?.second
    }

    fun overlayObjectIsNotAttached(id: String): Boolean {
        return attachedViewList.none { it.tag == id }
    }

    fun overlayObjectIsAttached(id: String): Boolean {
        return attachedViewList.any { it.tag == id }
    }

    fun clearAll() {
        attachedViewList.clear()
        animations.clear()
        viewIdToIdMap.clear()
    }


}