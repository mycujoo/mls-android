package tv.mycujoo.mls.entity.msc

import tv.mycujoo.mls.entity.actions.Action

data class TimeLineItem(val streamOffset: Long, val action: Action) {


}