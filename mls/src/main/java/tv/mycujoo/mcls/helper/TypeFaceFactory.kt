package tv.mycujoo.mcls.helper

import android.content.res.AssetManager
import android.graphics.Typeface

/**
 * Implementation of ITypeFaceFactory to create custom fonts for parsing SVG into view
 * @see ITypeFaceFactory
 */
class TypeFaceFactory(private val assetManager: AssetManager) : ITypeFaceFactory {
    override fun createFromAsset(font: String): Typeface {
        return Typeface.createFromAsset(assetManager, font)
    }
}