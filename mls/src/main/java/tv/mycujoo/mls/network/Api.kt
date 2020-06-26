package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.actions.HighlightAction
import tv.mycujoo.mls.entity.msc.TimeLineItem
import tv.mycujoo.mls.entity.actions.ActionWrapper

interface Api {

    fun getTimeLineMarkers(): List<TimeLineItem>

    fun getHighlights(): List<HighlightAction>

    fun getActions(): List<ActionWrapper>

}
