package tv.mycujoo.mls.entity.actions

import com.google.gson.annotations.SerializedName

class ShowScoreboardOverlayAction : AbstractAction() {
    @SerializedName("description")
     override var description =
        "Shows a visual on top of the video player. Specialized for football scoreboards."

    lateinit var colorLeft: String
    lateinit var colorRight: String

    lateinit var abbrLeft: String
    lateinit var abbrRight: String

    lateinit var scoreLeft: String
    lateinit var scoreRight: String

}