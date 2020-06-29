package tv.mycujoo.mls.helper

import android.content.Context
import android.widget.ImageView
import com.caverock.androidsvg.SVGImageView

class OverlayFactory {

    companion object {
        fun create(
            context: Context
        ): SVGImageView {
            val svgImageView = SVGImageView(context)
            svgImageView.scaleType = ImageView.ScaleType.FIT_XY
            return svgImageView
        }
    }
}