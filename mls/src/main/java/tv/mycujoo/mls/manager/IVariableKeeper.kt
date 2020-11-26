package tv.mycujoo.mls.manager

import tv.mycujoo.domain.entity.SetVariableEntity
import java.util.*

interface IVariableKeeper {

    fun getValue(name: String): String

    fun createTimerPublisher(name: String)
    fun observeOnTimer(timerName: String, callback: (Pair<String, String>) -> Unit)
    fun getTimerNames(): List<String>
    fun notifyTimers(timerVariables: HashMap<String, TimerVariable>)

    fun createVariablePublisher(name: String)
    fun observeOnVariable(variableName: String, callback: (Pair<String, String>) -> Unit)
    fun getVariableNames(): List<String>
    fun notifyVariables(timerVariables: HashMap<String, SetVariableEntity>)
}
