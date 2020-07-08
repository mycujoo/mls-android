package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.actions.HighlightAction
import tv.mycujoo.mls.entity.msc.TimeLineItem

interface Api {

    fun getTimeLineMarkers(): List<TimeLineItem>

    fun getHighlights(): List<HighlightAction>

}
