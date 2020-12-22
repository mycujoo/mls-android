package tv.mycujoo.mls.helper

import android.graphics.Typeface
import com.caverock.androidsvg.SVGExternalFileResolver
import java.util.*

class SVGAssetResolver(private val typeFaceFactory: ITypeFaceFactory) : SVGExternalFileResolver() {
    override fun resolveFont(fontFamily: String?, fontWeight: Int, fontStyle: String?): Typeface? {
        return try {
            if (fontFamily == null) {
                return null
            }

            val lowerCasedFontName = fontFamily.toLowerCase(Locale.ENGLISH)
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
        const val ROBOTO_MONO_BOLD = "robotomono-bold"
        const val ROBOTO_MONO_REGULAR = "robotomono-regular"
        const val ROBOTO_MONO_BOLD_FILE_NAME = "robomono-b.ttf"
        const val ROBOTO_MONO_REGULAR_FILE_NAME = "robomono-r.ttf"

    }
}