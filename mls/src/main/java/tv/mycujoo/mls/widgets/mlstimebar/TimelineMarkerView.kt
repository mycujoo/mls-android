package tv.mycujoo.mls.widgets.mlstimebar

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**region Fields*/
    private val anchorView: View = View(context)
    private val frameLayout: FrameLayout
    private val textView: TextView
    var bgColor = ""
    private lateinit var idlingResource: CountingIdlingResource
    /**endregion */

    /**region Initializing*/
    init {
        anchorView.id = View.generateViewId()
        val anchorLayoutParams = LayoutParams(
            1,
            1
        )
        anchorLayoutParams.leftToLeft = id
        addView(anchorView, anchorLayoutParams)

        frameLayout = FrameLayout(context)
        frameLayout.id = View.generateViewId()
        val frameLayoutLayoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        frameLayoutLayoutParams.leftToLeft = anchorView.id
        frameLayoutLayoutParams.topToTop = this.id
        frameLayoutLayoutParams.rightToRight = anchorView.id
        addView(frameLayout, frameLayoutLayoutParams)

        frameLayout.background =
            ContextCompat.getDrawable(context, R.drawable.shape_highlight_marker_bg)
        val density = resources.displayMetrics.density
        val verticalPadding: Int = (4 * density).toInt()
        val sidePadding: Int = (8 * density).toInt()

        frameLayout.setPadding(sidePadding, verticalPadding, sidePadding, verticalPadding)


        textView = TextView(context)
        textView.id = View.generateViewId()
        val layoutParams = FrameLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.gravity = Gravity.CENTER
        frameLayout.addView(textView, layoutParams)
    }

    fun initialize(color: String) {
        bgColor = color
        frameLayout.doOnLayout {
            frameLayout.background.colorFilter = PorterDuffColorFilter(
                Color.parseColor(color),
                PorterDuff.Mode.SRC_ATOP
            )
        }


        visibility = View.GONE
    }
    /**endregion */


    /**region Functions*/


    /**
     * expects input from 0 to screen-width to position the time-line marker
     */
    @UiThread
    private fun setPosition(relationalPosition: Int) {
        if (this::idlingResource.isInitialized) {
            idlingResource.increment()
        }

        var relationalPosition = relationalPosition

        if ((textView.width / 2) > relationalPosition) {
            relationalPosition += (textView.width / 2)
        }

        if ((parent as View).width - (textView.width / 2) < relationalPosition) {
            relationalPosition -= (textView.width / 2)
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)

        constraintSet.setMargin(
            anchorView.id,
            ConstraintSet.START,
            relationalPosition
        )

        constraintSet.applyTo(this)

        doOnLayout {
            visibility = View.VISIBLE

            if (this::idlingResource.isInitialized) {
                if (idlingResource.isIdleNow.not()) {
                    idlingResource.decrement()
                }
            }
        }
    }

    fun removeMarkerTexts() {
        visibility = View.GONE
    }

    fun setMarkerTexts(titles: List<String>, position: Int) {
        if (titles.isEmpty()) {
            removeMarkerTexts()
        }
        visibility = View.VISIBLE
        textView.visibility = View.VISIBLE
        textView.text = titles.first()
        textView.doOnLayout {
            setPosition(position)
        }

        if (ColorUtils.isColorBright(bgColor)) {
            textView.setTextColor(Color.parseColor("#000000"))
        } else {
            textView.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }
    /**endregion */

    /**region Test helper*/
    fun setIdlingResource(idlingResource: CountingIdlingResource) {
        this.idlingResource = idlingResource
    }
    /**endregion */
}