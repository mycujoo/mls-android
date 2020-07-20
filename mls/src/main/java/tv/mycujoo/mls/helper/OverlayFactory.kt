package tv.mycujoo.mls.helper

import android.content.Context
import android.view.View
import android.widget.ImageView
import tv.mycujoo.mls.widgets.ProportionalImageView
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory {

    companion object {
        fun create(
            context: Context,
            tag: String,
            size: Pair<Float, Float>
        ): ProportionalImageView {

            val proportionalImageView = ProportionalImageView(context, size.first, size.second)
            proportionalImageView.id = View.generateViewId()
            proportionalImageView.tag = tag
            proportionalImageView.adjustViewBounds = true
            proportionalImageView.scaleType = ImageView.ScaleType.FIT_START
            return proportionalImageView
        }

        fun createScaffold(
            context: Context,
            tag: String,
            size: Pair<Float, Float>
        ): ScaffoldView {
            val scaffoldView = ScaffoldView(size.first, size.second, context)
            scaffoldView.id = View.generateViewId()
            scaffoldView.tag = tag

            return scaffoldView

        }
    }
}