package tv.mycujoo.mls.helper

import android.content.Context
import android.view.View
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory {

    companion object {
        fun createScaffoldView(
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