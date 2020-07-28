package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.model.ScreenTimerFormat.*
import tv.mycujoo.mls.widgets.AdjustTimerEntity
import tv.mycujoo.mls.widgets.StartTimerEntity

class TimerCore(
    val name: String,
    private val offset: Long,
    private val format: ScreenTimerFormat,
    private val direction: ScreenTimerDirection,
    startValue: Long,
    private val step: Long,
    private val capValue: Long
) {
    //todo [wip] [mind capValue!]
    private var currentTime = startValue
    private var isTicking = false


    fun getFormattedTime(): String {
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
        return if (time >= 6000L) {
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
        if (direction == ScreenTimerDirection.UP && currentTime <= capValue) {
            return true
        }
        if (direction == ScreenTimerDirection.DOWN && currentTime >= capValue) {
            return true
        }
        return false
    }

    fun start(
        timerRelay: BehaviorRelay<String>,
        dispatcher: CoroutineScope
    ) {
        if (isTicking) {
            return
        }
        isTicking = true

        dispatcher.launch {
            while (isTicking) {
                delay(step)

                if (direction == ScreenTimerDirection.UP) {
                    currentTime += step
                } else {
                    currentTime -= step
                }

                if (timeIsWithinCapValue()) {
                    timerRelay.accept(getFormattedTime())
                }
            }
        }
    }

    fun resume() {
        isTicking = true
    }

    fun pause() {
        isTicking = false
    }

    fun adjustTime(
        time: Long,
        timerRelay: BehaviorRelay<String>,
        dispatcher: CoroutineScope
    ) {
        dispatcher.launch {
            currentTime = time
            timerRelay.accept(getFormattedTime())
        }
    }

    fun tuneWithStartEntity(
        now: Long,
        startTimerEntity: StartTimerEntity,
        timerRelay: BehaviorRelay<String>,
        dispatcher: CoroutineScope
    ) {
        dispatcher.launch {
            currentTime = now
            val dif = currentTime - startTimerEntity.offset
            currentTime = (dif / 1000L) * step
            timerRelay.accept(getFormattedTime())
        }
    }

    fun tuneWithAdjustEntity(
        now: Long,
        adjustTimerEntity: AdjustTimerEntity,
        timerRelay: BehaviorRelay<String>,
        dispatcher: CoroutineScope
    ) {
        dispatcher.launch {
            val passedTimeFromAdjust = now - adjustTimerEntity.offset
            currentTime = (passedTimeFromAdjust / 1000L) * step
            currentTime += adjustTimerEntity.value
            timerRelay.accept(getFormattedTime())
        }
    }

}