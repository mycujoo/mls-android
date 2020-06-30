package tv.mycujoo.mls.helper

import android.content.Context
import android.view.View
import android.widget.ImageView
import tv.mycujoo.mls.widgets.ProportionalImageView

class OverlayFactory {

    companion object {
        fun create(
            context: Context,
            size: Pair<Float, Float>
        ): ProportionalImageView {

            val proportionalImageView = ProportionalImageView(context, size.first, size.second)
            proportionalImageView.id = View.generateViewId()
            proportionalImageView.adjustViewBounds = true
            proportionalImageView.scaleType = ImageView.ScaleType.FIT_START
            return proportionalImageView
        }
    }
}