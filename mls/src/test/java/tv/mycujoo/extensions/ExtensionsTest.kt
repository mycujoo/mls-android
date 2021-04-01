package tv.mycujoo.extensions

import org.junit.Test
import tv.mycujoo.mcls.extensions.length
import kotlin.test.assertEquals

class ExtensionsTest {

    @Test
    fun `test Long type length`() {
        assertEquals(1, 0L.length())
        assertEquals(1, 1L.length())
        assertEquals(2, 10L.length())
        assertEquals(3, 100L.length())
        assertEquals(4, 1000L.length())
        assertEquals(5, 10000L.length())
        assertEquals(6, 100000L.length())
        assertEquals(7, 1000000L.length())
        assertEquals(8, 10000000L.length())
        assertEquals(9, 100000000L.length())
        assertEquals(10, 1000000000L.length())
        assertEquals(11, 10000000000L.length())
        assertEquals(12, 100000000000L.length())
        assertEquals(13, 1000000000000L.length())
        assertEquals(14, 10000000000000L.length())
        assertEquals(15, 100000000000000L.length())
    }
}