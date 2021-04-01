package tv.mycujoo.mcls.helper

import android.content.res.AssetManager
import android.graphics.Typeface

class TypeFaceFactory(private val assetManager: AssetManager) : ITypeFaceFactory {
    override fun createFromAsset(font: String): Typeface {
        return Typeface.createFromAsset(assetManager, font)
    }
}