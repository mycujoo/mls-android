package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay

data class TimerTwin(
    val timerCore: TimerCore,
    val timerRelay: BehaviorRelay<String>
)