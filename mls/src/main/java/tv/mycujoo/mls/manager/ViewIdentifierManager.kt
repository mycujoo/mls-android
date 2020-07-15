package tv.mycujoo.mls.manager

import android.animation.ObjectAnimator
import android.view.View

class ViewIdentifierManager {
    var viewIdToIdMap = mutableMapOf<String, Int>()
    var viewIdToAnimationMap = mutableMapOf<Int, ObjectAnimator>()

    var animations = ArrayList<ObjectAnimator>()

    val attachedOverlayList: ArrayList<String> = ArrayList()
    val attachedAnimationList: ArrayList<String> = ArrayList()


    fun storeViewId(view: View, customId: String) {
        viewIdToIdMap[customId] = view.id
    }

    fun getViewId(customId: String): Int? {
        return viewIdToIdMap[customId]
    }

    fun storeAnimation(viewId: Int, animation: ObjectAnimator) {
        viewIdToAnimationMap[viewId] = animation
    }

    fun getAnimationByViewId(viewId: Int): ObjectAnimator? {
        return if (viewIdToAnimationMap.containsKey(viewId)) {
            viewIdToAnimationMap[viewId]
        } else null
    }

    fun addAnimation(objectAnimator: ObjectAnimator) {
        animations.add(objectAnimator)
    }

    fun getAnimations(): List<ObjectAnimator> {
        return animations
    }

    fun getAnimationByCustomId(customId: String?): ObjectAnimator? {
        if (customId.isNullOrEmpty()) {
            return null
        }
        return getViewId(customId)?.let { getAnimationByViewId(it) }

    }

}