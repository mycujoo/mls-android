package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.Variable

class ScaffoldView @JvmOverloads constructor(
    private val widthPercentage: Float = -1F,
    private val heightPercentage: Float = -1F,
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var svgString: String
    private lateinit var variablePlaceHolder: Map<String, String>

    private var proportionalImageView: ProportionalImageView =
        ProportionalImageView(context, widthPercentage, heightPercentage, attrs, defStyleAttr)

    init {
        proportionalImageView.scaleType = ImageView.ScaleType.FIT_START
        proportionalImageView.adjustViewBounds = true


        addView(
            proportionalImageView, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )

    }


    fun setSVG(svg: SVG) {
        proportionalImageView.setSVG(svg)
    }

    fun setSVGSource(svgString: String) {
        this.svgString = svgString
    }

    fun setScaleType(scaleType: ImageView.ScaleType) {
        proportionalImageView.scaleType = scaleType
    }

    fun setVariable(variable: Variable) {
        if (this::variablePlaceHolder.isInitialized.not() || this::svgString.isInitialized.not()) {
            return
        }
        if (variablePlaceHolder.containsValue(variable.name)) {
            variablePlaceHolder.entries.firstOrNull { it.value == variable.name }?.let { entry ->
                svgString = svgString.replace(entry.key, variable.value.toString())
                setSVG(SVG.getFromString(svgString))
            }

        }

    }


    fun setVariablePlaceHolder(variablePlaceHolders: Map<String, String>) {
        this.variablePlaceHolder = variablePlaceHolders
    }
}