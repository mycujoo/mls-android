package tv.mycujoo.mcls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class VariableTranslator @Inject constructor(private val dispatcher: CoroutineScope) {
    private val variableTripleList = ArrayList<VariableTriple>()

    fun createVariableTripleIfNotExisted(variableName: String) {
        if (variableTripleList.none { it.variableName == variableName }) {
            variableTripleList.add(
                VariableTriple(
                    variableName,
                    arrayListOf(),
                    BehaviorRelay.create()
                )
            )
        }
    }

    fun emitNewValue(variableName: String, variableValue: Any) {
        variableTripleList.firstOrNull { it.variableName == variableName }
            ?.let { variableRelay ->
                variableRelay.variableRelay.accept(Pair(variableName, variableValue))
            }
    }

    fun observe(variableName: String, callback: (Pair<String, Any>) -> Unit) {
        variableTripleList.firstOrNull { it.variableName == variableName }
            ?.let { variableTriple ->
                variableTriple.callbackList.add(callback)
            }
    }

    fun getValue(key: String): Any? {
        return variableTripleList.firstOrNull { it.variableName == key }?.variableRelay?.value?.second
    }

    fun setVariablesNameValueIfDifferent(variablesTillNow: HashMap<String, Any>) {
        dispatcher.launch {
            variableTripleList.forEach { variableRelay ->
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
