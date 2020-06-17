package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.entity.TimeLineItem
import tv.mycujoo.mls.entity.actions.ActionWrapper

interface Api {

    fun getTimeLineMarkers(): List<TimeLineItem>

    fun getHighlights(): List<HighlightAction>

    fun getActions(): List<ActionWrapper>

}
