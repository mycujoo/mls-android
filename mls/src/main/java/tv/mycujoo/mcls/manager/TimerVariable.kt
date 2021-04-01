package tv.mycujoo.mcls.manager

import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat

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

    override fun start(startTimer: TimerEntity.StartTimer, now: Long) {
        commands.add(startTimer)
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
        val sorted =
            commands.sortedWith(compareBy<TimerEntity> { it.offset }.thenByDescending { it.priority })
        var delta = startValue
        var isTicking = false
        sorted
            .forEach { timerEntity ->
                when (timerEntity) {
                    is TimerEntity.StartTimer -> {
                        if (isTicking) {
                            return@forEach
                        }
                        isTicking = true
                        if (direction == ScreenTimerDirection.UP) {
                            delta += now - timerEntity.offset
                        } else {
                            delta -= now - timerEntity.offset
                        }
                    }
                    is TimerEntity.PauseTimer -> {
                        if (isTicking.not()) {
                            return@forEach
                        }
                        isTicking = false
                        if (direction == ScreenTimerDirection.UP) {
                            delta -= now - timerEntity.offset
                        } else {
                            delta += now - timerEntity.offset
                        }
                    }
                    is TimerEntity.AdjustTimer -> {
                        if (direction == ScreenTimerDirection.UP) {
                            delta = startValue + now - timerEntity.offset + timerEntity.value
                        } else {
                            delta = startValue - (now - timerEntity.offset + timerEntity.value)
                        }
                    }
                    is TimerEntity.SkipTimer -> {
                        if (direction == ScreenTimerDirection.UP) {
                            delta += timerEntity.value
                        } else {
                            delta -= timerEntity.value
                        }

                    }
                }

            }
        currentTime = delta
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