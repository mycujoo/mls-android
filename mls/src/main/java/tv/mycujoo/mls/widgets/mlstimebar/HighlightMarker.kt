package tv.mycujoo.mls.widgets.mlstimebar

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import tv.mycujoo.mls.R
import tv.mycujoo.mls.utils.ColorUtils


class HighlightMarker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /**region Fields*/
    var bgColor = ""
    /**endregion */

    /**region Initializing*/
    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        background = ContextCompat.getDrawable(context, R.drawable.shape_highlight_marker_bg)
        val density = resources.displayMetrics.density
        val verticalPadding: Int = (4 * density).toInt()
        val sidePadding: Int = (8 * density).toInt()

        setPadding(sidePadding, verticalPadding, sidePadding, verticalPadding)
    }

    fun initialize(color: String) {
        bgColor = color
        background.colorFilter = PorterDuffColorFilter(
            Color.parseColor(color),
            PorterDuff.Mode.SRC_ATOP
        )

        visibility = View.GONE
    }
    /**endregion */


    /**region Functions*/


    /**
     * expects input from 0L to 1L
     */
    fun setPosition(relationalPosition: Int) {
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

    fun removeTexts() {
        children.forEach { removeView(it) }
    }

    fun addHighlightTexts(titles: List<String>) {
        removeTexts()
        titles.forEach {
            addView(TextView(context).apply {
                text = it
                gravity = Gravity.CENTER


                if (ColorUtils.isColorBright(bgColor)) {
                    setTextColor(Color.parseColor("#000000"))
                } else {
                    setTextColor(Color.parseColor("#FFFFFF"))
                }

            })
        }
    }

    /**endregion */
}