package tv.mycujoo.mls.manager

import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.PauseTimerEntity
import tv.mycujoo.mls.entity.SkipTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat

class TimerVariable(
    val name: String,
    private val format: ScreenTimerFormat,
    private val direction: ScreenTimerDirection,
    private val startValue: Long,
    private val capValue: Long
) : ITimer {
    private var currentTime = startValue
    private val step: Long
    private val commands = ArrayList<TimerEntity>()

    init {
        if (currentTime == -1L) {
            currentTime = 0L
        }
        if (startValue != -1L) {
            currentTime = startValue
        }
        step = if (direction == ScreenTimerDirection.UP) {
            1000L
        } else {
            -1000L
        }
    }


    /**region Over-ridden functions*/
    override fun getTime(): String {
        return when (format) {
            ScreenTimerFormat.MINUTES_SECONDS -> {
                if (timeIsWithinCapValue()) {
                    getTimeInMinutesSecondFormat(currentTime)
                } else {
                    getTimeInMinutesSecondFormat(capValue)
                }
            }
            ScreenTimerFormat.SECONDS -> {
                if (timeIsWithinCapValue()) {
                    getTimeInSecondFormat(currentTime)
                } else {
                    getTimeInSecondFormat(capValue)
                }
            }
            ScreenTimerFormat.UNKNOWN -> {
                ""
            }
        }
    }

    override fun start(startTimerEntity: StartTimerEntity, now: Long) {
        commands.add(TimerEntity.StartTimer(startTimerEntity))
        recalculate(now)
    }

    override fun pause(pauseTimerEntity: PauseTimerEntity, now: Long) {
        commands.add(TimerEntity.PauseTimer(pauseTimerEntity))
        recalculate(now)
    }


    override fun adjust(adjustTimerEntity: AdjustTimerEntity, now: Long) {
        commands.add(TimerEntity.AdjustTimer(adjustTimerEntity))
        recalculate(now)
    }

    override fun skip(skipTimerEntity: SkipTimerEntity, now: Long) {
        commands.add(TimerEntity.SkipTimer(skipTimerEntity))
        recalculate(now)
    }

    /**endregion */

    /**region Private functions*/
    private fun recalculate(now: Long) {
        var startOffset = 0L
        var pauseOffset = 0L
        var adjustEntity: AdjustTimerEntity? = null
        var skipTimerEntity: SkipTimerEntity? = null
        commands.forEach {
            when (it) {
                is TimerEntity.StartTimer -> {
                    startOffset = it.startTimerEntity.offset

                }
                is TimerEntity.PauseTimer -> {
                    pauseOffset = it.pauseTimerEntity.offset
                }
                is TimerEntity.AdjustTimer -> {
                    adjustEntity = it.adjustTimerEntity
                }

                is TimerEntity.SkipTimer -> {
                    skipTimerEntity = it.skipTimerEntity
                }

                else -> {
                }
            }
        }
        when (direction) {
            ScreenTimerDirection.UP -> {
                currentTime = startValue + now - startOffset

                if (pauseOffset != 0L) {
                    currentTime -= now - pauseOffset
                }

                adjustEntity?.let {
                    currentTime = startValue + now - it.offset + it.value
                }

                skipTimerEntity?.let {
                    currentTime += it.value
                }

            }
            ScreenTimerDirection.DOWN -> {
                currentTime = startValue - (now - startOffset)

                if (pauseOffset != 0L) {
                    currentTime += now - pauseOffset
                }

                adjustEntity?.let {
                    currentTime = startValue - (now - it.offset + it.value)
                }

                skipTimerEntity?.let {
                    currentTime -= it.value
                }
            }
        }
    }

    private fun getTimeInMinutesSecondFormat(time: Long): String {
        return if (time >= 60000L) {
            // 1 minute or more, so M is present
            val minutes = time / 60000L
            val seconds = ((time % 60000L) / 1000L).toString()
            "$minutes:${seconds.padStart(2, '0')}"
        } else {
            val seconds = (time / 1000L).toString()

            "0:${seconds.padStart(2, '0')}"
        }
    }

    private fun getTimeInSecondFormat(time: Long) = (time / 1000L).toString()

    private fun timeIsWithinCapValue(): Boolean {
        if (capValue == -1L) {
            return true
        }

        if (direction == ScreenTimerDirection.UP && currentTime <= capValue) {
            return true
        }
        if (direction == ScreenTimerDirection.DOWN && currentTime >= capValue) {
            return true
        }
        return false
    }
    /**endregion */


}