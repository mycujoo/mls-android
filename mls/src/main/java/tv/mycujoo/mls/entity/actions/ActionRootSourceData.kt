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
                                    "scoreLeft" -> {
                                        showScoreboardOverlayAction.scoreLeft = metadata.value!!
                                    }
                                    "scoreRight" -> {
                                        showScoreboardOverlayAction.scoreRight = metadata.value!!
                                    }
                                    else -> {
                                    }
                                }

                            }
                        }
                        actionsList.add(showScoreboardOverlayAction)

                    }
                    ABSTRACT_ACTION_SHOW_TIME_LINE_MARKER_ID -> {
                        val showTimeLineMarkerAction = ShowTimeLineMarkerAction()
                        action.metadata?.forEach { metadata ->
                            if (metadata.value.isNullOrEmpty()) {
                                Log.e("ActionRootSourceData", "given null value for meta!")
                                return
                            }
                            when (metadata.key) {
                                "tag" -> {
                                    showTimeLineMarkerAction.tag = metadata.value!!
                                }
                                "color" -> {
                                    showTimeLineMarkerAction.color = metadata.value!!
                                }
                                else -> {
                                }
                            }
                        }
                        actionsList.add(showTimeLineMarkerAction)
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