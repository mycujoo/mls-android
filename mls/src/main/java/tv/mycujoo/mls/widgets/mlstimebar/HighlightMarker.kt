package tv.mycujoo.mls.widgets.mlstimebar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.highlight_marker_layout.view.*
import tv.mycujoo.mls.R


class HighlightMarker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    init {

        LayoutInflater.from(context).inflate(R.layout.highlight_marker_layout, this, true)


    }


    /**
     * expect input from 0L to 1L
     */
    fun setPosition(relationalPosition: Int) {
//        setPadding(((parent as ConstraintLayout).width * relationalPosition).toInt(), 0, 0, 0)

        val constraintSet = ConstraintSet()
        constraintSet.clone(parent as ConstraintLayout)

        this.post {
            constraintSet.setMargin(
                this.id,
                ConstraintSet.START,
                relationalPosition - (measuredWidth / 2)
            )
            constraintSet.applyTo(parent as ConstraintLayout)
            visibility = View.VISIBLE


        }

    }

    fun setText(title: String?) {
        highlightMarker_textView.text = title
    }
}