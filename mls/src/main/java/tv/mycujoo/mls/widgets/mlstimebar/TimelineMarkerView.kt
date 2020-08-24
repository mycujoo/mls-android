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
import androidx.annotation.UiThread
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.mls.R
import tv.mycujoo.mls.utils.ColorUtils


class TimelineMarkerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /**region Fields*/
    var bgColor = ""
    private lateinit var idlingResource: CountingIdlingResource
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
     * expects input from 0 to screen-width to position the time-line marker
     */
    @UiThread
    fun setPosition(relationalPosition: Int) {
        if (this::idlingResource.isInitialized) {
            idlingResource.increment()
        }
        visibility = View.INVISIBLE

        doOnLayout {
            val parentLayout = parent as ConstraintLayout
            val constraintSet = ConstraintSet()
            constraintSet.clone(parentLayout)

            if (parentLayout.width - (measuredWidth / 2) < relationalPosition) {
                // marker should stick to the end
                constraintSet.connect(
                    this.id,
                    ConstraintSet.END,
                    parentLayout.id,
                    ConstraintSet.END
                )
                constraintSet.clear(this.id, ConstraintSet.START)
            } else {
                constraintSet.connect(
                    this.id,
                    ConstraintSet.START,
                    parentLayout.id,
                    ConstraintSet.START
                )
                constraintSet.setMargin(
                    this.id,
                    ConstraintSet.START,
                    relationalPosition - (width / 2)
                )
                constraintSet.clear(this.id, ConstraintSet.END)
            }

            constraintSet.applyTo(parentLayout)

            doOnLayout {
                visibility = View.VISIBLE

                if (this::idlingResource.isInitialized) {
                    if (idlingResource.isIdleNow.not()) {
                        idlingResource.decrement()
                    }
                }
            }
        }
    }

    fun removeMarkerTexts() {
        removeAllViews()
    }

    fun setMarkerTexts(titles: List<String>) {
        removeMarkerTexts()
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

    /**region Test helper*/
    fun setIdlingResource(idlingResource: CountingIdlingResource) {
        this.idlingResource = idlingResource
    }
    /**endregion */
}