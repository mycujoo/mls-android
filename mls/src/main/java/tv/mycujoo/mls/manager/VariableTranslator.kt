package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class VariableTranslator(private val dispatcher: CoroutineScope) {
    private val variableRelayList = ArrayList<VariableRelay>()

    fun createVariableLiveEventIfNotExisted(variableName: String) {

        dispatcher.launch {
            if (variableRelayList.none { it.variableName == variableName }) {
                variableRelayList.add(VariableRelay(variableName, BehaviorRelay.create()))
            }
        }
    }

    fun emitNewValue(variableName: String, variableValue: Any) {
        variableRelayList.firstOrNull { it.variableName == variableName }
            ?.let { variableRelay ->
                variableRelay.variableValue.accept(Pair(variableName, variableValue))
            }
    }

    fun observe(variableName: String, callback: (Pair<String, Any>) -> Unit) {
        dispatcher.launch {
            variableRelayList.firstOrNull { it.variableName == variableName }
                ?.let { variableRelay ->
                    variableRelay.variableValue.subscribe { callback.invoke(it) }
                }
        }
    }

    fun getValue(key: String): Any? {
        return variableRelayList.firstOrNull { it.variableName == key }?.variableValue?.value?.second
    }

    fun setVariablesNameValueIfDifferent(variablesTillNow: HashMap<String, Any>) {
        dispatcher.launch {
            variableRelayList.forEach { variableRelay ->
                if (variablesTillNow.containsKey(variableRelay.variableName)) {
                    if (variableRelay.variableValue.value?.second == null || variablesTillNow[variableRelay.variableName] != variableRelay.variableValue.value.second) {
                        variableRelay.variableValue.accept(
                            Pair(
                                variableRelay.variableName,
                                variablesTillNow[variableRelay.variableName]!!
                            )
                        )
                    }
                }
            }

        }
    }


}
