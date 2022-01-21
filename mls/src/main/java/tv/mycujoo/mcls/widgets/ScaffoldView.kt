package tv.mycujoo.mcls.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.UiThread
import com.caverock.androidsvg.SVG
import timber.log.Timber

class ScaffoldView @JvmOverloads constructor(
    widthPercentage: Float = -1F,
    heightPercentage: Float = -1F,
    context: Context,
    attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var svgStringBuilder = StringBuilder()
    private lateinit var variablePlaceHolder: List<String>
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


    @UiThread
    fun setSVG(svg: SVG) {
        proportionalImageView.setSVG(svg)
    }

    fun setSVGSource(svgString: String) {
        svgStringBuilder = StringBuilder(svgString)
    }

    fun setScaleType(scaleType: ImageView.ScaleType) {
        proportionalImageView.scaleType = scaleType
    }


    fun setVariablePlaceHolder(variablePlaceHolders: List<String>) {
        this.variablePlaceHolder = variablePlaceHolders
        this.latestVariableValue = mutableMapOf()
    }

    fun initialVariables(updatedPair: Pair<String, Any>) {
        if (this::variablePlaceHolder.isInitialized.not() || this::latestVariableValue.isInitialized.not()) {
            return
        }
        if (!variablePlaceHolder.contains(updatedPair.first) || updatedPair.second == "") {
            return
        }

        latestVariableValue[updatedPair.first] = updatedPair.second

    }

    fun onVariableUpdated(
        updatedPair: Pair<String, Any>
    ) {
        if (this::variablePlaceHolder.isInitialized.not() || this::latestVariableValue.isInitialized.not()) {
            return
        }

        if (!variablePlaceHolder.contains(updatedPair.first)) {
            return
        }

        latestVariableValue[updatedPair.first] = updatedPair.second

        val currentSvg = StringBuilder(svgStringBuilder)

        if (currentSvg.isNotEmpty()) {
            variablePlaceHolder.filter { latestVariableValue.contains(it) }.forEach { entry ->
                latestVariableValue[entry]?.let { value ->
                    val start = currentSvg.indexOf(entry)
                    if (start > -1) {
                        currentSvg.replace(start, start + entry.length, value.toString())
                    }
                }
            }

            post {
                setSVG(SVG.getFromString(currentSvg.toString()))
            }
        }
    }
}