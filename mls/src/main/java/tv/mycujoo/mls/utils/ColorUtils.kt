package tv.mycujoo.mls.utils

import android.graphics.Color
import android.util.Log
import androidx.core.graphics.ColorUtils

class ColorUtils {
    companion object {
        private const val LUMINANCE_DEGREE = 0.5

        fun isColorBright(color: String?): Boolean {
            return try {
                ColorUtils.calculateLuminance(Color.parseColor(color)) > LUMINANCE_DEGREE
            } catch (e: Exception) {
                Log.e("ColorUtils", "Given string can not be parsed in to color")
                false
            }
        }


        fun isColorBright(color: Int?): Boolean {
            return color?.let { ColorUtils.calculateLuminance(it) > LUMINANCE_DEGREE } ?: false
        }

        fun toARGB(color: String): String {
            if (color.length == 5) {
                val color1 = color.replace(
                    Regex("#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])"),
                    "#$1$1$2$2$3$3$4$4"
                )
                return toARGB(color1)
            }
            if (color.length == 9) {
                return "#".plus(color.substring(7, 9)).plus(color.substring(1, 7))
            } else return color

        }

    }
}