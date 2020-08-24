package tv.mycujoo.mls.manager

import tv.mycujoo.mls.entity.*

sealed class TimerEntity {
    data class CreateTimer(val createTimerEntity: CreateTimerEntity) : TimerEntity()
    data class StartTimer(val startTimerEntity: StartTimerEntity) : TimerEntity()
    data class PauseTimer(val pauseTimerEntity: PauseTimerEntity) : TimerEntity()
    data class AdjustTimer(val adjustTimerEntity: AdjustTimerEntity) : TimerEntity()
    data class SkipTimer(val skipTimerEntity: SkipTimerEntity) : TimerEntity()
    data class KillTimer(val timerName: String) : TimerEntity()
}