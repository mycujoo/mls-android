package tv.mycujoo

import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource
import java.util.concurrent.TimeoutException

class IdlingResourceHelper constructor(countingIdlingResource: CountingIdlingResource) {

    init {
        IdlingResource = countingIdlingResource
        startTime = System.currentTimeMillis()
    }

    companion object {
        private lateinit var IdlingResource: CountingIdlingResource
        private var startTime: Long = System.currentTimeMillis()
        private var timeoutMillisecond = 10000L
    }

    fun setTimeoutLimit(timeLimitInMilliSeconds: Long) {
        timeoutMillisecond = timeLimitInMilliSeconds
    }

    fun waitUntilIdle() {
        startTime = System.currentTimeMillis()
        var duration: Long
        while (!IdlingResource.isIdleNow) {
            duration = System.currentTimeMillis() - startTime

            if (duration > timeoutMillisecond) {
                throw TimeoutException("IdlingResource Took Too Long To Idling, " +
                        "Current Time Limit ${timeoutMillisecond}ms, " +
                        "but current time ${duration}ms")
            }
            Thread.sleep(500)
        }
    }
}

