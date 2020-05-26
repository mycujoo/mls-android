package tv.mycujoo.mls.entity.actions

import tv.mycujoo.mls.entity.LayoutPosition
import tv.mycujoo.mls.widgets.ViewGuideline


class ShowScoreboardOverlayAction : AbstractAction(), ViewGuideline {
    override var description =
        "Shows a visual on top of the video player. Specialized for football scoreboards."

    lateinit var colorLeft: String
    lateinit var colorRight: String

    lateinit var abbrLeft: String
    lateinit var abbrRight: String

    lateinit var scoreLeft: String
    lateinit var scoreRight: String

    override var position: LayoutPosition = LayoutPosition.BOTTOM_RIGHT
    override var dismissible: Boolean = false
    override var dismissIn: Long = -1L

}