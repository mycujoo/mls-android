package tv.mycujoo.mls.manager

import android.util.Log
import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.PauseTimerEntity
import tv.mycujoo.mls.entity.SkipTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerDirection.DOWN
import tv.mycujoo.mls.model.ScreenTimerDirection.UP
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.model.ScreenTimerFormat.*

/**
 * TimerCore is where a Timer times, direction, format and other attributes are stored.
 *
 * Mind that applying any entity on this class will make it act as Alive.
 *
 * Note:
 * This class is tested through TimerKeeper tests.
 */
class TimerCore(
    val name: String,
    private val offset: Long,
    private val format: ScreenTimerFormat,
    private val direction: ScreenTimerDirection,
    private val startValue: Long,
    private val capValue: Long
) {
    private var currentTime = startValue
    private var isAlive = true
    private var isTicking = true
    private val step: Long


    init {
        if (direction == UP) {
            step = 1000L
        } else {
            step = -1000L
        }
    }


    fun getFormattedTime(): String {

        if (isAlive.not()) {
            Log.w("TimerCore", "A timer which is not alive is trying to be accessed!")
            return ""
        }


        return when (format) {
            MINUTES_SECONDS -> {
                if (timeIsWithinCapValue()) {
                    getTimeInMinutesSecondFormat(currentTime)
                } else {
                    getTimeInMinutesSecondFormat(capValue)
                }
            }
            SECONDS -> {
                if (timeIsWithinCapValue()) {
                    getTimeInSecondFormat(currentTime)
                } else {
                    getTimeInSecondFormat(capValue)
                }
            }
            UNKNOWN -> {
                ""
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

        if (direction == UP && currentTime <= capValue) {
            return true
        }
        if (direction == DOWN && currentTime >= capValue) {
            return true
        }
        return false
    }


    fun notifyObservers(timerTwin: TimerTwin) {
        timerTwin.timerRelay.accept(getFormattedTime())
    }


    fun setTime(
        startTimerEntity: StartTimerEntity,
        now: Long
    ) {
        if (isTicking.not()){
            return
        }
        isAlive = true
        when (direction) {
            UP -> {
                currentTime = now + startValue
                val dif = currentTime - startTimerEntity.offset
                currentTime = (dif / 1000L) * step
            }
            DOWN -> {
                currentTime = now - startValue
                val dif = currentTime - startTimerEntity.offset
                currentTime = (dif / 1000L) * step
            }
        }

    }

    fun setTime(pauseTimerEntity: PauseTimerEntity, now: Long) {
        isTicking = false
    }

    fun setTime(adjustTimerEntity: AdjustTimerEntity, now: Long) {
        if (isTicking.not()){
            return
        }
        isAlive = true
        val passedTimeFromAdjust = now - adjustTimerEntity.offset
        currentTime = (passedTimeFromAdjust / 1000L) * step
        currentTime += adjustTimerEntity.value
    }

    fun setTime(skipTimerEntity: SkipTimerEntity) {
        if (isTicking.not()){
            return
        }
        isAlive = true
        currentTime += skipTimerEntity.value
    }

    fun kill() {
        isAlive = false
    }


}