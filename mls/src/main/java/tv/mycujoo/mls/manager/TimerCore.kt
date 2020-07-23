package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.model.ScreenTimerFormat.*

class TimerCore(
    val name: String,
    private val offset: Long,
    private val format: ScreenTimerFormat,
    private val direction: ScreenTimerDirection,
    private val startValue: Long,
    private val step: Long,
    private val capValue: Long
) {
    //todo [wip] [mind capValue!]
    private var currentTime = startValue
    private var isTicking = false


    fun getFormattedTime(): String {
        return when (format) {
            MINUTES_SECONDS -> {
                getTimeInMinutesSecondFormat()
            }
            SECONDS -> {
                (currentTime / 1000L).toString()
            }
            UNKNOWN -> {
                ""
            }
        }
    }

    private fun getTimeInMinutesSecondFormat(): String {
        return if (currentTime >= 6000L) {
            // 1 minute or more, so M is present
            val minutes = currentTime / 60000L
            val seconds = ((currentTime % 60000L) / 1000L).toString()
            "$minutes:${seconds.padStart(2, '0')}"
        } else {
            val seconds = (currentTime / 1000L).toString()

            "0:${seconds.padStart(2, '0')}"
        }
    }

    fun start(
        timerRelay: BehaviorRelay<String>,
        dispatcher: CoroutineScope
    ) {
        if (isTicking) {
            return
        }
        isTicking = true
        currentTime -= step
        dispatcher.launch {
            while (isTicking) {
                currentTime += step
                timerRelay.accept(getFormattedTime())
                delay(step)
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

    fun fineTuneTime(
        now: Long,
        givenOffset: Long,
        timerRelay: BehaviorRelay<String>,
        dispatcher: CoroutineScope
    ) {
        dispatcher.launch {
            currentTime = now
            val dif = currentTime - givenOffset
            currentTime = (dif / 1000L) * step
            timerRelay.accept(getFormattedTime())
        }
    }

}