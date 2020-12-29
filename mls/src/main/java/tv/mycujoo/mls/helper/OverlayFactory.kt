package tv.mycujoo.mls.helper

import android.content.Context
import android.util.Log
import android.view.View
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory : IOverlayFactory {

    override fun createScaffoldView(
        context: Context,
        showOverlayAction: Action.ShowOverlayAction,
        variableTranslator: VariableTranslator,
        variableKeeper: IVariableKeeper
    ): ScaffoldView {

        val size = showOverlayAction.viewSpec!!.size!!
        val scaffoldView = ScaffoldView(size.first, size.second, context)
        scaffoldView.id = View.generateViewId()
        scaffoldView.tag = showOverlayAction.customId ?: showOverlayAction.id

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

            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            scaffoldView.setSVG(svg)
            scaffoldView.setSVGSource(showOverlayAction.svgData.svgString!!)


        } catch (e: Exception) {
            Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
        }

        return scaffoldView

    }
}