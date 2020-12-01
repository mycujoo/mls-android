package tv.mycujoo.mls.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsTest {
    @Test
    fun `given null should return 0`() {

        val result = StringUtils.getNumberOfViewers(null)


        assertEquals("0", result)
    }

    @Test
    fun `given invalid string should return 0`() {

        val result = StringUtils.getNumberOfViewers("-")


        assertEquals("0", result)
    }

    @Test
    fun `given any number below 1000, should return the same`() {
        val resultOf1 = StringUtils.getNumberOfViewers("1")
        val resultOf10 = StringUtils.getNumberOfViewers("10")
        val resultOf100 = StringUtils.getNumberOfViewers("100")
        val resultOf999 = StringUtils.getNumberOfViewers("999")


        assertEquals("1", resultOf1)
        assertEquals("10", resultOf10)
        assertEquals("100", resultOf100)
        assertEquals("999", resultOf999)
    }

    @Test
    fun `given any number between 1000 & 999999, should return rounded + appending K`() {
        val resultOf1000 = StringUtils.getNumberOfViewers("1000")
        val resultOf1051 = StringUtils.getNumberOfViewers("1051")
        val resultOf1100 = StringUtils.getNumberOfViewers("1100")
        val resultOf2000 = StringUtils.getNumberOfViewers("2000")
        val resultOf999999 = StringUtils.getNumberOfViewers("999999")


        assertEquals("1K", resultOf1000)
        assertEquals("1.1K", resultOf1051)
        assertEquals("1.1K", resultOf1100)
        assertEquals("2K", resultOf2000)
        assertEquals("1.000K", resultOf999999)
    }

    @Test
    fun `given any number greater than 1000000, should return rounded + appending M`() {
        val resultOf1000000 = StringUtils.getNumberOfViewers("1000000")
        val resultOf1000001 = StringUtils.getNumberOfViewers("1000001")
        val resultOf1000010 = StringUtils.getNumberOfViewers("1000010")
        val resultOf1000100 = StringUtils.getNumberOfViewers("1000100")
        val resultOf1001000 = StringUtils.getNumberOfViewers("1001000")
        val resultOf1010000 = StringUtils.getNumberOfViewers("1010000")
        val resultOf1100000 = StringUtils.getNumberOfViewers("1100000")


        assertEquals("1M", resultOf1000000)
        assertEquals("1M", resultOf1000001)
        assertEquals("1M", resultOf1000010)
        assertEquals("1M", resultOf1000100)
        assertEquals("1M", resultOf1001000)
        assertEquals("1M", resultOf1010000)
        assertEquals("1.1M", resultOf1100000)
    }

    @Test
    fun `given invalid segment name, should return empty string`() {
        assertEquals("", StringUtils.getSegmentTimeStamp(""))
        assertEquals("", StringUtils.getSegmentTimeStamp("a"))
        assertEquals("", StringUtils.getSegmentTimeStamp("1"))
        assertEquals(
            "",
            StringUtils.getSegmentTimeStamp("https://dc9jagk60w3y3mt6171f-b03c88.p5cdn.com")
        )
        assertEquals(
            "",
            StringUtils.getSegmentTimeStamp("https://dc9jagk60w3y3mt6171f-b03c88.p5cdn.com/shervin/ckfieucs8000y0114j6pdcucf/1080p/1080_segment_")
        )
    }

    @Test
    fun `given valid segment name, should return timestamp`() {
        assertEquals("1601048398", StringUtils.getSegmentTimeStamp("segment_1601048398"))
        assertEquals(
            "1601048398",
            StringUtils.getSegmentTimeStamp("https://dc9jagk60w3y3mt6171f-b03c88.p5cdn.com/shervin/ckfieucs8000y0114j6pdcucf/1080p/1080_segment_1601048398_00004.ts")
        )
    }

}