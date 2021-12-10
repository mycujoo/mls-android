package tv.mycujoo.mcls.helper

import android.content.Context
import android.util.Log
import android.view.View
import com.caverock.androidsvg.SVG
import dagger.hilt.android.qualifiers.ApplicationContext
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mcls.manager.IVariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.widgets.ScaffoldView
import javax.inject.Inject

/**
 * Create overlay, which is the view used for displaying Annotation Action on screen.
 *
 *
 * @param context context to create View from
 * @param variableKeeper provides observables to for variables defined/used in Overlay
 */
class OverlayFactory @Inject constructor(
    @ApplicationContext val context: Context,
    val variableKeeper: IVariableKeeper,
    val variableTranslator: VariableTranslator
) : IOverlayFactory {

    /**
     * @param showOverlayAction Action which provides data of to be created view
     */
    override fun createScaffoldView(
        showOverlayAction: Action.ShowOverlayAction,
    ): ScaffoldView {

        val size = showOverlayAction.viewSpec!!.size!!
        val scaffoldView = ScaffoldView(size.first, size.second, context)
        scaffoldView.id = View.generateViewId()
        scaffoldView.tag = showOverlayAction.customId

        scaffoldView.setVariablePlaceHolder(showOverlayAction.placeHolders)


        showOverlayAction.placeHolders.forEach { entry ->
            // VALUE of place-holder, is the KEY in the set_variable map
            variableKeeper.getTimerNames().firstOrNull {
                it == entry
            }?.let {
                variableKeeper.observeOnTimer(entry) { scaffoldView.onVariableUpdated(it) }
                scaffoldView.initialVariables(Pair(entry, variableKeeper.getValue(entry)))
            }

            variableKeeper.getVariableNames().find { it == entry }?.let {
                variableKeeper.observeOnVariable(entry) { scaffoldView.onVariableUpdated(it) }
                scaffoldView.initialVariables(Pair(entry, variableKeeper.getValue(entry)))
            }
        }

        try {
            val svg: SVG
            var rawString = showOverlayAction.svgData!!.svgString!!
            showOverlayAction.placeHolders.forEach { placeHolder ->
                val value = variableKeeper.getValue(placeHolder)
                rawString =
                    rawString.replace(placeHolder, value)
            }

            svg = SVG.getFromString(rawString)

            scaffoldView.setSVG(svg)
            scaffoldView.setSVGSource(showOverlayAction.svgData.svgString!!)


        } catch (e: Exception) {
            Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
        }

        return scaffoldView

    }
}