package tv.mycujoo

import androidx.test.espresso.idling.CountingIdlingResource

fun waitUntilIdle(countingIdlingResource: CountingIdlingResource, millisecond: Long? = 500) {
    while (!countingIdlingResource.isIdleNow) {
        Thread.sleep(millisecond ?: 500)
    }
}
