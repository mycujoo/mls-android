package tv.mycujoo.mls.helper

import android.content.res.AssetManager
import android.graphics.Typeface
import com.caverock.androidsvg.SVGExternalFileResolver

class SVGAssetResolver(private val assetManager: AssetManager) : SVGExternalFileResolver() {
    override fun resolveFont(fontFamily: String?, fontWeight: Int, fontStyle: String?): Typeface? {
        return try {
            Typeface.createFromAsset(assetManager, "$fontFamily.ttf")
        } catch (ignored: RuntimeException) {
            null
        }
    }
}