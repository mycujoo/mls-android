package tv.mycujoo.mls.entity.actions

import tv.mycujoo.mls.entity.LayoutPosition
import tv.mycujoo.mls.widgets.ViewGuideline

class ShowAnnouncementOverlayAction : AbstractAction(), ViewGuideline {

    lateinit var color: String
    lateinit var line1: String
    lateinit var line2: String
    lateinit var imageUrl: String

    override var position: LayoutPosition = LayoutPosition.BOTTOM_RIGHT
    override var dismissible: Boolean = false
    override var dismissIn: Long = -1L
}