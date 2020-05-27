package tv.mycujoo.mls.widgets

import tv.mycujoo.mls.entity.LayoutPosition

interface ViewGuideline {
    var viewId : String
    var position: LayoutPosition
    var dismissible: Boolean
    var dismissIn: Long
}
