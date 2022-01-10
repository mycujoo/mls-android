package tv.mycujoo.mcls.helper

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Implementation of ITypeFaceFactory to create custom fonts for parsing SVG into view
 * @see ITypeFaceFactory
 */
class TypeFaceFactory @Inject constructor(
    @ApplicationContext private val context: Context
) : ITypeFaceFactory {
    override fun createFromAsset(font: String): Typeface {
        return Typeface.createFromAsset(context.assets, font)
    }
}
