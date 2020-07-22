package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay

data class TimerRelay(
    val timerCore: TimerCore,
    val timerValue: BehaviorRelay<String>
)