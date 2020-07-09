package tv.mycujoo.mls.utils

import android.graphics.Color
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import tv.mycujoo.mls.utils.ColorUtils.Companion.isColorBright

class ColorUtilsTest {
    @Test
    fun whiteColorTest() {
        val white = isColorBright(Color.WHITE)
        val whiteString = isColorBright("#FFFFFF")
        val brightBlue = isColorBright("#AED6F1")

        val mediumBlue = isColorBright("#85C1E9")
        val darkBlue = isColorBright("#2E86C1")
        val black = isColorBright("#000000")



        assertTrue(white)
        assertTrue(whiteString)
        assertTrue(brightBlue)
        assertFalse(mediumBlue)
        assertFalse(darkBlue)
        assertFalse(black)
    }
}