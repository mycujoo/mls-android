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

    }
}