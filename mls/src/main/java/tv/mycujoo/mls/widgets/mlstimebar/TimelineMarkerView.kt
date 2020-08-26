package tv.mycujoo.mls.widgets.mlstimebar

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
    private var counter = 0
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
    private fun setPosition(relationalPosition: Int) {
        if (this::idlingResource.isInitialized) {
            idlingResource.increment()
        }

        val parentLayout = parent as ConstraintLayout
        val constraintSet = ConstraintSet()
        constraintSet.clone(parentLayout)

        if (parentLayout.width - (width / 2) < relationalPosition) {
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

    fun removeMarkerTexts() {
        if (counter > 0) {
            counter = 0
            removeAllViewsInLayout()
            visibility = View.INVISIBLE

        }
    }

    fun setMarkerTexts(titles: List<String>, toInt: Int) {
        if (counter > 0) {
            val t = object : OnHierarchyChangeListener {
                override fun onChildViewAdded(parent: View?, child: View?) {
                }

                override fun onChildViewRemoved(parent: View?, child: View?) {
                    if ((parent as ViewGroup).childCount == 0) {
                        addTitles(titles, toInt)
                    }
                }
            }
            setOnHierarchyChangeListener(t)

            removeMarkerTexts()
        } else {
            addTitles(titles, toInt)
        }

    }

    private fun addTitles(titles: List<String>, toInt: Int) {
        counter = titles.size

        val t = object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                if ((parent as ViewGroup).childCount == titles.size) {
                    setPosition(toInt)
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {

            }
        }

        setOnHierarchyChangeListener(t)


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