package tv.mycujoo.mls.utils

import org.junit.Test
import kotlin.test.assertEquals

class TimeUtilsTest {
    @Test
    fun `convert relative time to absolute time`() {
        val actionAbsTime = 1605609890000L
        val windowAbsoluteStartTime = 1605609882000L

        assertEquals(
            8000L,
            TimeUtils.convertRelativeTimeToAbsolute(windowAbsoluteStartTime, actionAbsTime)
        )
    }

    @Test
    fun `action before current window should return -1L`() {
        val actionAbsTime = 1605609881000L
        val windowAbsoluteStartTime = 1605609882000L

        assertEquals(
            -1L,
            TimeUtils.convertRelativeTimeToAbsolute(windowAbsoluteStartTime, actionAbsTime)
        )
    }
}