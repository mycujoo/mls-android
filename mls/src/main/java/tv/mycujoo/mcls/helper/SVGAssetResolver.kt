package tv.mycujoo.mcls.helper

import android.graphics.Typeface
import com.caverock.androidsvg.SVGExternalFileResolver
import java.util.*

/**
 * Implementation to SVGExternalFileResolver which is required by AndroidSVG library, in order to parse custom fonts.
 * @see SVGExternalFileResolver
 */
class SVGAssetResolver(private val typeFaceFactory: ITypeFaceFactory) : SVGExternalFileResolver() {
    override fun resolveFont(fontFamily: String?, fontWeight: Int, fontStyle: String?): Typeface? {
        return try {
            if (fontFamily == null) {
                return null
            }

            //Important! lower case the font name as name of font files can NOT be upper case in assets
            val lowerCasedFontName = fontFamily.lowercase(Locale.ENGLISH)
            if (lowerCasedFontName.contains(ROBOTO_MONO_BOLD)) {
                return typeFaceFactory.createFromAsset(ROBOTO_MONO_BOLD_FILE_NAME)
            }
            if (lowerCasedFontName.contains(ROBOTO_MONO_REGULAR)) {
                return typeFaceFactory.createFromAsset(ROBOTO_MONO_REGULAR_FILE_NAME)
            }
            return null


        } catch (ignored: RuntimeException) {
            null
        }
    }

    companion object {
        /**
         * Name of fonts and their related files, including the types (.ttf) in asset directory
         */
        const val ROBOTO_MONO_BOLD = "robotomono-bold"
        const val ROBOTO_MONO_REGULAR = "robotomono-regular"
        const val ROBOTO_MONO_BOLD_FILE_NAME = "robomono-b.ttf"
        const val ROBOTO_MONO_REGULAR_FILE_NAME = "robomono-r.ttf"

    }
}