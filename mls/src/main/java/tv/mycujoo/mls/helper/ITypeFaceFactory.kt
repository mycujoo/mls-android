package tv.mycujoo.mls.helper

import android.graphics.Typeface

interface ITypeFaceFactory {
    fun createFromAsset(font: String): Typeface
}
