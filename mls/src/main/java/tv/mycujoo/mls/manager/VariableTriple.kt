package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay

data class VariableTriple(
    val variableName: String,
    val callbackList: ArrayList<(Pair<String, Any>) -> Unit>,
    val variableRelay: BehaviorRelay<Pair<String, Any>>
) {
    init {
        variableRelay.subscribe { pair ->
            callbackList.forEach { callback ->
                callback.invoke(pair)
            }
        }
    }
}