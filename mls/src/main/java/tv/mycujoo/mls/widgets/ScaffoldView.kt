package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.mls.manager.VariableTranslator

class ScaffoldView @JvmOverloads constructor(
    widthPercentage: Float = -1F,
    heightPercentage: Float = -1F,
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
        post { proportionalImageView.setSVG(svg) }
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

    fun onVariableUpdated(
        variableTranslator: VariableTranslator
    ) {
        if (this::variablePlaceHolder.isInitialized.not() || this::svgString.isInitialized.not()) {
            return
        }

        var svgString = this.svgString
        variablePlaceHolder.forEach { entry ->
            val value = variableTranslator.getValue(entry.value)
            value?.let {
                svgString = svgString.replace(entry.key, value.toString())
            }
        }
        setSVG(SVG.getFromString(svgString))
    }


}