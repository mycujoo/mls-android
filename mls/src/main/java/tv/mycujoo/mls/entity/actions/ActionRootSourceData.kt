package tv.mycujoo.mls.entity.actions

import android.util.Log
import com.google.gson.annotations.SerializedName

class ActionRootSourceData {
    fun build() {
        actionIdentifiers?.forEach { action ->
            action.actionAbstractId?.let {
                when (it) {
                    // 0 -> ShowScoreboardOverlayAction
                    "0" -> {
                        val showScoreboardOverlayAction = ShowScoreboardOverlayAction()
                        action.metadata?.let { metadataList ->
                            metadataList.forEach { metadata ->
                                if (metadata.value.isNullOrEmpty()) {
                                    Log.e("ActionRootSourceData", "given null value for meta!")
                                    return
                                }
                                when (metadata.key) {
                                    "colorLeft" -> {
                                        showScoreboardOverlayAction.colorLeft = metadata.value!!
                                    }
                                    "colorRight" -> {
                                        showScoreboardOverlayAction.colorRight = metadata.value!!
                                    }
                                    "abbrLeft" -> {
                                        showScoreboardOverlayAction.abbrLeft = metadata.value!!
                                    }
                                    "abbrRight" -> {
                                        showScoreboardOverlayAction.abbrRight = metadata.value!!
                                    }
                                    else -> {
                                    }
                                }

                            }
                        }
                        actionsList.add(showScoreboardOverlayAction)

                    }
                    else -> {
                    }
                }
            }
        }
    }

    val actionsList: ArrayList<AbstractAction> = ArrayList()

    @SerializedName("id")
    var id: String? = null

    @SerializedName("actions")
    var actionIdentifiers: List<ActionIdentifier>? = null


}