package tv.mycujoo.mls.manager

import android.util.Log
import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class VariableTranslator(private val dispatcher: CoroutineScope) {
    private val variableRelayList = ArrayList<VariableTriple>()

    fun createVariableLiveEventIfNotExisted(variableName: String) {

        if (variableRelayList.none { it.variableName == variableName }) {
            variableRelayList.add(
                VariableTriple(
                    variableName,
                    arrayListOf(),
                    BehaviorRelay.create()
                )
            )
        }
    }

    fun emitNewValue(variableName: String, variableValue: Any) {
        variableRelayList.firstOrNull { it.variableName == variableName }
            ?.let { variableRelay ->
                variableRelay.variableRelay.accept(Pair(variableName, variableValue))
            }
    }

    fun observe(variableName: String, callback: (Pair<String, Any>) -> Unit) {
        variableRelayList.firstOrNull { it.variableName == variableName }
            ?.let { variableTriple ->
                variableTriple.callbackList.add(callback)
            }
    }

    fun getValue(key: String): Any? {
        return variableRelayList.firstOrNull { it.variableName == key }?.variableRelay?.value?.second
    }

    fun setVariablesNameValueIfDifferent(variablesTillNow: HashMap<String, Any>) {
        dispatcher.launch {
            variableRelayList.forEach { variableRelay ->
                if (variablesTillNow.containsKey(variableRelay.variableName)) {
                    if (variableRelay.variableRelay.value?.second == null || variablesTillNow[variableRelay.variableName] != variableRelay.variableRelay.value.second) {
                        variableRelay.variableRelay.accept(
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
