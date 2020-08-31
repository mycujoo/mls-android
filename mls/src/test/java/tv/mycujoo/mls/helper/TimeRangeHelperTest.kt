package tv.mycujoo.mls.helper

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeRangeHelperTest {

    @Test
    fun `given out of range timer-marker, should return false`() {
        assertFalse(TimeRangeHelper.isInRange(100F, 200))
        assertFalse(TimeRangeHelper.isInRange(189F, 200))
        assertFalse(TimeRangeHelper.isInRange(211F, 200))
    }

    @Test
    fun `given in range timer-marker, should return false`() {
        assertTrue(TimeRangeHelper.isInRange(200F, 200))
    }

    @Test
    fun `given lower range timer-marker, should return false`() {
        assertTrue(TimeRangeHelper.isInRange(190F, 200))
    }

    @Test
    fun `given higher range timer-marker, should return false`() {
        assertTrue(TimeRangeHelper.isInRange(210F, 200))
    }
}