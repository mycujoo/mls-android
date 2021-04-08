package tv.mycujoo.mcls.helper

import android.graphics.Typeface

interface ITypeFaceFactory {
    fun createFromAsset(font: String): Typeface
}
