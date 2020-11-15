package tv.mycujoo.mls.manager

import java.util.*

interface ITimerKeeper {

    fun getTimerRelayList(): List<TimerTwin>
    fun observe(timerName: String, callback: (Pair<String, String>) -> Unit)
    fun getValue(name: String): String
    fun notify(timerVariables: HashMap<String, TimerVariable>)
    fun getTimerNames() : List<String>
}
