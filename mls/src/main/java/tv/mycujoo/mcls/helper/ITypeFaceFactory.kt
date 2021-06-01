package tv.mycujoo.mcls.helper

import android.graphics.Typeface

/**
 * Contract for creating typeface which will be used in visualizing overlays.
 * Custom fonts (typefaces) are used in overlays.
 */
interface ITypeFaceFactory {
    fun createFromAsset(font: String): Typeface
}
