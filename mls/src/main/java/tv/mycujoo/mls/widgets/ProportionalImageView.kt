package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import com.caverock.androidsvg.SVGImageView


class ProportionalImageView @JvmOverloads constructor(
    context: Context,
    private val widthPercentage: Float = -1F,
    private val heightPercentage: Float = -1F,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SVGImageView(context, attrs, defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d = drawable
        if (d != null) {
            val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
            val screenHeight = MeasureSpec.getSize(heightMeasureSpec)

            if (widthPercentage > 0F) {
                val desiredWidth = (screenWidth / 100) * widthPercentage
                val h = screenWidth * d.intrinsicHeight / d.intrinsicWidth
                setMeasuredDimension(desiredWidth.toInt(), h)
            } else if (heightPercentage > 0F) {
                val desiredHeight = (screenHeight / 100) * heightPercentage
                val w = screenHeight * d.intrinsicWidth / d.intrinsicHeight
                setMeasuredDimension(w, desiredHeight.toInt())
            }

        } else super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}