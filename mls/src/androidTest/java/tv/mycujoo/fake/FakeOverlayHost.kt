package tv.mycujoo.fake

import android.content.Context
import android.util.AttributeSet
import android.view.View
import tv.mycujoo.mls.widgets.OverlayHost

class FakeOverlayHost @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : OverlayHost(context, attrs, defStyleAttr) {

    var lastRemovedView: View? = null

    override fun removeView(view: View?) {
        lastRemovedView = view
        super.removeView(view)
    }
}