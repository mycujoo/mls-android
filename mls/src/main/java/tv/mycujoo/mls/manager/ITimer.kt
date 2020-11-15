package tv.mycujoo.mls.manager

import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.PauseTimerEntity
import tv.mycujoo.mls.entity.SkipTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity

interface ITimer {

    fun getTime(): String
    fun start(startTimerEntity: StartTimerEntity, now: Long)
    fun pause(pauseTimerEntity: PauseTimerEntity, now: Long)
    fun adjust(adjustTimerEntity: AdjustTimerEntity, now: Long)
    fun skip(skipTimerEntity: SkipTimerEntity, now: Long)
}
