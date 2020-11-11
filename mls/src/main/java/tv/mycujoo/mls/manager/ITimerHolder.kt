package tv.mycujoo.mls.manager

import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.model.MutablePair

interface ITimerHolder {
    fun notifyTimers(timers: ArrayList<Set<MutablePair<CreateTimerEntity, String>>>)
}