package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.domain.entity.SetVariableEntity

class VariableKeeper(private val dispatcher: CoroutineScope) : IVariableKeeper {


    private val timerPublisherMap = HashMap<String, BehaviorRelay<String>>()
    private val variablePublisherMap = HashMap<String, BehaviorRelay<String>>()


    override fun getTimerNames(): List<String> {
        return timerPublisherMap.keys.toList()
    }

    override fun getVariableNames(): List<String> {
        return variablePublisherMap.keys.toList()
    }

    fun createTimerPublisher(name: String) {
        if (timerPublisherMap.containsKey(name)) {
            return
        }
        timerPublisherMap[name] = (BehaviorRelay.createDefault(""))
    }

    fun createVariablePublisher(name: String) {
        if (variablePublisherMap.containsKey(name)) {
            return
        }
        variablePublisherMap[name] = (BehaviorRelay.createDefault(""))
    }

    override fun observeOnTimer(timerName: String, callback: (Pair<String, String>) -> Unit) {
        dispatcher.launch {
            timerPublisherMap[timerName]?.let { behaviorRelay ->
                behaviorRelay.subscribe {
                    callback.invoke(Pair(timerName, it))
                }
            }
        }
    }

    override fun observeOnVariable(variableName: String, callback: (Pair<String, String>) -> Unit) {
        dispatcher.launch {
            variablePublisherMap[variableName]?.let { behaviorRelay ->
                behaviorRelay.subscribe {
                    callback.invoke(Pair(variableName, it))
                }
            }
        }
    }


    override fun getValue(name: String): String {
        return if (timerPublisherMap.containsKey(name)) {
            timerPublisherMap[name]?.value ?: ""
        } else {
            variablePublisherMap[name]?.value ?: ""
        }
    }

    override fun notifyTimers(timerVariables: HashMap<String, TimerVariable>) {
        timerPublisherMap.forEach { e ->
            timerVariables[e.key]?.let { timerVariable ->
                e.value.accept(timerVariable.getTime())
            }
        }
    }

    override fun notifyVariables(setVariableEntities: HashMap<String, SetVariableEntity>) {
        variablePublisherMap.forEach { e ->
            setVariableEntities[e.key]?.let { setEntityVariable ->
                e.value.accept(setEntityVariable.variable.printValue())
            }
        }
    }
}