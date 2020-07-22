package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.Variable

class ScaffoldView @JvmOverloads constructor(
    widthPercentage: Float = -1F,
    heightPercentage: Float = -1F,
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var svgString: String
    private lateinit var variablePlaceHolder: Map<String, String>
    private lateinit var latestVariableValue: MutableMap<String, Any>

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
        this.latestVariableValue = mutableMapOf()
    }

    fun onVariableUpdated(
        updatedPair: Pair<String, Any>
    ) {
        if (this::variablePlaceHolder.isInitialized.not() || this::latestVariableValue.isInitialized.not() || this::svgString.isInitialized.not()) {
            return
        }

        if (!variablePlaceHolder.containsValue(updatedPair.first)) {
            return
        }

        latestVariableValue[updatedPair.first] = updatedPair.second

        var svgString = this.svgString
        variablePlaceHolder.filter { latestVariableValue.contains(it.value) }.forEach { entry ->
            latestVariableValue[entry.value]?.let { value ->
                svgString =
                    svgString.replace(entry.key, value.toString())
            }
        }

        setSVG(SVG.getFromString(svgString))
    }


}