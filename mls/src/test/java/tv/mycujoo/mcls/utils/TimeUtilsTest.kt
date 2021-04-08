package tv.mycujoo.mcls.utils

import org.junit.Test
import kotlin.test.assertEquals

class TimeUtilsTest {
    @Test
    fun `convert action absolute time to relative time based on window-start-time`() {
        val actionInWindowAbsTime = 1605609890000L // 8000L in window
        val actionOufOfWindowAbsTime = 1605609880000L // -2000L out of window
        val windowAbsoluteStartTime = 1605609882000L

        assertEquals(
            8000L,
            TimeUtils.calculateOffset(windowAbsoluteStartTime, actionInWindowAbsTime)
        )
        assertEquals(
            -2000L,
            TimeUtils.calculateOffset(windowAbsoluteStartTime, actionOufOfWindowAbsTime)
        )
    }
}