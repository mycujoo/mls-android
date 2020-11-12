package tv.mycujoo.mls.manager

import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.model.MutablePair
import java.util.HashMap

interface ITimerKeeper {

    fun getTimerRelayList(): List<TimerTwin>
    fun observe(timerName: String, callback: (Pair<String, String>) -> Unit)
    fun getValue(name: String): String
    fun notify(timers: HashMap<String, MutablePair<CreateTimerEntity, String>>)
}
