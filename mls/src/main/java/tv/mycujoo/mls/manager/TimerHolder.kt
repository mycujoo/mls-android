package tv.mycujoo.mls.manager

import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.model.MutablePair
import java.util.ArrayList

class TimerHolder : ITimerHolder {

    override fun notifyTimers(timers: ArrayList<Set<MutablePair<CreateTimerEntity, String>>>) {

    }
}