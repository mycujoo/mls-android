package tv.mycujoo.mls.manager

import android.view.View

class ViewIdentifierManager {
    var viewIdToIdMap = mutableMapOf<String, Int>()

    fun storeViewId(view: View, actionViewIdentifier: String) {
        viewIdToIdMap[actionViewIdentifier] = view.id
    }

    fun getViewIdentifier(actionViewIdentifier: String): Int? {
        return viewIdToIdMap[actionViewIdentifier]
    }
}