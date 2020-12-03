package tv.mycujoo.mls.utils

import org.junit.Test
import tv.mycujoo.mls.utils.MathUtils.Companion.convertToEpochInMS
import tv.mycujoo.mls.utils.MathUtils.Companion.safeLongToInt
import kotlin.test.assertEquals

class MathUtilsTest {

    @Test
    fun `convert Long to Int test`() {
        assertEquals(0, safeLongToInt(0L))
        assertEquals(10, safeLongToInt(10L))
        assertEquals(100, safeLongToInt(100L))
    }

    @Test(expected = ArithmeticException::class)
    fun `convert Long to Int test overflow exception`() {
        assertEquals(0, safeLongToInt(Long.MAX_VALUE))
    }

    @Test
    fun `convert to epoch in MS`() {
        assertEquals(1605609882000, convertToEpochInMS(1605609882L))
        assertEquals(1605609882000, convertToEpochInMS(1605609882000L))
    }
}