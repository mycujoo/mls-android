package tv.mycujoo.mcls.utils

import android.graphics.Color
import junit.framework.Assert.*
import org.junit.Test
import tv.mycujoo.mcls.utils.ColorUtils.Companion.isColorBright

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

    @Test
    fun colorRGBtoARGBTest() {
        val validRGB = "#fff"

        val convertedToRGBA = ColorUtils.toARGB(validRGB)
        assertEquals(validRGB, convertedToRGBA)
    }

    @Test
    fun colorRGBAtoAARRGGBBTest() {
        val validRGB = "#fff0"

        val convertedToRGBA = ColorUtils.toARGB(validRGB)
        assertEquals("#00ffffff", convertedToRGBA)
    }

    @Test
    fun colorRRGGBBtoARGBTest() {
        val validRGB = "#ffffff"

        val convertedToRGBA = ColorUtils.toARGB(validRGB)
        assertEquals(validRGB, convertedToRGBA)
    }

    @Test
    fun colorRRGGBBAAtoARGBTest() {
        val validRGBA = "#ffffff00"

        val convertedToARGB = ColorUtils.toARGB(validRGBA)
        assertEquals("#00ffffff", convertedToARGB)
    }
}