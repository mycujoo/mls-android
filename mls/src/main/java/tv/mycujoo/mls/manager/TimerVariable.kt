package tv.mycujoo.mls.manager

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

    override fun start(statTimer: TimerEntity.StartTimer, now: Long) {
        commands.add(statTimer)
        recalculate(now)
    }

    override fun pause(pauseTimer: TimerEntity.PauseTimer, now: Long) {
        commands.add(pauseTimer)
        recalculate(now)
    }


    override fun adjust(adjustTimer: TimerEntity.AdjustTimer, now: Long) {
        commands.add(adjustTimer)
        recalculate(now)
    }

    override fun skip(skipTimer: TimerEntity.SkipTimer, now: Long) {
        commands.add(skipTimer)
        recalculate(now)
    }

    /**endregion */

    /**region Private functions*/
    private fun recalculate(now: Long) {
        var startOffset = 0L
        var pauseOffset = 0L
        var adjustTimer: TimerEntity.AdjustTimer? = null
        var skipTimer: TimerEntity.SkipTimer? = null
        commands.forEach {
            when (it) {
                is TimerEntity.StartTimer -> {
                    startOffset = it.offset

                }
                is TimerEntity.PauseTimer -> {
                    pauseOffset = it.offset
                }
                is TimerEntity.AdjustTimer -> {
                    adjustTimer = it
                }

                is TimerEntity.SkipTimer -> {
                    skipTimer = it
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

                adjustTimer?.let {
                    currentTime = startValue + now - it.offset + it.value
                }

                skipTimer?.let {
                    currentTime += it.value
                }

            }
            ScreenTimerDirection.DOWN -> {
                currentTime = startValue - (now - startOffset)

                if (pauseOffset != 0L) {
                    currentTime += now - pauseOffset
                }

                adjustTimer?.let {
                    currentTime = startValue - (now - it.offset + it.value)
                }

                skipTimer?.let {
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