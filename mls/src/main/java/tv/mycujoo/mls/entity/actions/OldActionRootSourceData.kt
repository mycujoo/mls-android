package tv.mycujoo.mls.entity.actions

import android.util.Log
import com.google.gson.annotations.SerializedName

@Deprecated("Use Action instead")
class OldActionRootSourceData {
    fun build() {
        actionSourceData?.forEach { action ->
            action.actionAbstractId?.let {
                when (it) {
                    // 0 -> ShowScoreboardOverlayAction
                    ABSTRACT_ACTION_SHOW_SCOREBOARD_OVERLAY_ID -> {
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
                                    "viewId" -> {
                                        showScoreboardOverlayAction.viewId = metadata.value!!
                                    }
                                    "dismissible" -> {
                                        showScoreboardOverlayAction.dismissible =
                                            metadata.value!!.toBoolean()
                                    }
                                    "dismissIn" -> {
                                        showScoreboardOverlayAction.dismissIn =
                                            metadata.value!!.toLong()
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

                    ABSTRACT_ACTION_SHOW_ANNOUNCEMENT_OVERLAY_ID -> {
                        val showAnnouncementOverlayAction = ShowAnnouncementOverlayAction()
                        action.metadata?.forEach { metadata ->
                            if (metadata.value.isNullOrEmpty()) {
                                Log.e("ActionRootSourceData", "given null value for meta!")
                                return
                            }
                            when (metadata.key) {
                                "color" -> {
                                    showAnnouncementOverlayAction.color = metadata.value!!
                                }
                                "line1" -> {
                                    showAnnouncementOverlayAction.line1 = metadata.value!!
                                }
                                "line2" -> {
                                    showAnnouncementOverlayAction.line2 = metadata.value!!
                                }
                                "imageUrl" -> {
                                    showAnnouncementOverlayAction.imageUrl = metadata.value!!
                                }
                                "viewId" -> {
                                    showAnnouncementOverlayAction.viewId = metadata.value!!
                                }
                                "dismissible" -> {
                                    showAnnouncementOverlayAction.dismissible =
                                        metadata.value!!.toBoolean()
                                }
                                "dismissIn" -> {
                                    showAnnouncementOverlayAction.dismissIn =
                                        metadata.value?.toLong() ?: -1L
                                }
                                else -> {
                                }
                            }
                        }
                        actionsList.add(showAnnouncementOverlayAction)
                    }

                    ABSTRACT_ACTION_COMMAND_ID -> {
                        val commandAction = CommandAction()
                        action.metadata?.forEach { metadata ->
                            when (metadata.key) {
                                "targetViewId" -> {
                                    commandAction.targetViewId = metadata.value!!
                                }
                                "verb" -> {
                                    commandAction.verb = metadata.value!!
                                }
                                "offset" -> {
                                    commandAction.offset = metadata.value?.toLong() ?: -1L
                                }
                                else -> {

                                }

                            }
                        }
                        actionsList.add(commandAction)
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

    @SerializedName("time")
    var time: Long? = null

    @SerializedName("actions")
    var actionSourceData: List<ActionSourceData>? = null


}