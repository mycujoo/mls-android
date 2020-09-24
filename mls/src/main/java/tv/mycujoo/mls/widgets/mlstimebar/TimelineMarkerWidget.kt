package tv.mycujoo.mls.widgets.mlstimebar

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class TimelineMarkerWidget(
    private val anchor: View,
    private val backgroundLayout: FrameLayout,
    private val timelineMarkerTextView: TextView
) {


    init {
        backgroundLayout.background.colorFilter = PorterDuffColorFilter(
            Color.parseColor("#ff0000"),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    fun removeMarkerTexts() {
        timelineMarkerTextView.text = null
        backgroundLayout.visibility = View.GONE
    }

    fun setMarkerTexts(titles: List<String>, position: Int) {
        backgroundLayout.visibility = View.VISIBLE

        val stringBuilder = StringBuilder()
        stringBuilder.append(titles.first())

        for (i in 1 until titles.size) {
            stringBuilder.append("\n")
            stringBuilder.append(titles[i])
        }
        timelineMarkerTextView.text = stringBuilder.toString()

        val parentLayout = anchor.parent as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        constraintSet.setMargin(
            anchor.id,
            ConstraintSet.START,
            position
        )

        constraintSet.applyTo(anchor.parent as ConstraintLayout)
    }
}