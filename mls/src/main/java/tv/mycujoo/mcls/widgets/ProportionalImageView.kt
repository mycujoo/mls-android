package tv.mycujoo.mcls.widgets

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
                val h = desiredWidth * d.intrinsicHeight / d.intrinsicWidth
                setMeasuredDimension(desiredWidth.toInt(), h.toInt())
            } else if (heightPercentage > 0F) {
                val desiredHeight = (screenHeight / 100) * heightPercentage
                val w = desiredHeight * d.intrinsicWidth / d.intrinsicHeight
                setMeasuredDimension(w.toInt(), desiredHeight.toInt())
            }

        } else super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}