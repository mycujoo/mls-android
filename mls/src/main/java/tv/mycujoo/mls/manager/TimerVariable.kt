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
    private var isTicking = true

    init {
        if (currentTime == -1L) {
            currentTime = 0L
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
        isTicking = true // only for re-starting
        calculate(startTimerEntity.offset, now)
    }

    override fun pause(pauseTimerEntity: PauseTimerEntity, now: Long) {
        isTicking = false
        calculate(pauseTimerEntity.offset, now)
    }

    private fun calculate(offset: Long, now: Long) {
        when (direction) {
            ScreenTimerDirection.UP -> {
                currentTime = now + startValue
                val dif = currentTime - offset
                currentTime = (dif / 1000L) * step
            }
            ScreenTimerDirection.DOWN -> {
                currentTime = now - startValue
                val dif = currentTime - offset
                currentTime = (dif / 1000L) * step
            }
        }
    }


    override fun adjust(adjustTimerEntity: AdjustTimerEntity, now: Long) {
        if (isTicking.not()) {
            return
        }
        val passedTimeFromAdjust = now - adjustTimerEntity.offset
        currentTime = (passedTimeFromAdjust / 1000L) * step
        currentTime += adjustTimerEntity.value
    }

    override fun skip(skipTimerEntity: SkipTimerEntity, now: Long) {
        if (isTicking.not()) {
            return
        }
        currentTime += skipTimerEntity.value
    }

    /**endregion */

    /**region Private functions*/
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