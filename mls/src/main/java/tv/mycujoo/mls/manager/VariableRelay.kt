package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay

data class VariableRelay(
    val variableName: String,
    val variableValue: BehaviorRelay<Pair<String, Any>>
)