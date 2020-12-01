package tv.mycujoo.fake

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class FakeOverlayHost @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var lastRemovedView: View? = null

    override fun removeView(view: View?) {
        lastRemovedView = view
        super.removeView(view)
    }
}