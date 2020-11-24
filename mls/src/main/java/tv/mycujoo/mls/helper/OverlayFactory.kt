package tv.mycujoo.mls.helper

import android.content.Context
import android.util.Log
import android.view.View
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory : IOverlayFactory {

    override fun createScaffoldView(
        context: Context,
        overlayEntity: OverlayEntity,
        variableTranslator: VariableTranslator,
        variableKeeper: IVariableKeeper
    ): ScaffoldView {

        val size = overlayEntity.viewSpec.size!!
        val scaffoldView = ScaffoldView(size.first, size.second, context)
        scaffoldView.id = View.generateViewId()
        scaffoldView.tag = overlayEntity.id

        scaffoldView.setVariablePlaceHolder(overlayEntity.variablePlaceHolders)


        overlayEntity.variablePlaceHolders.forEach { entry ->
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
            var rawString = overlayEntity.svgData!!.svgString!!
            overlayEntity.variablePlaceHolders.forEach { placeHolder ->
                var value = variableTranslator.getValue(placeHolder)
                if (value == null) {
                    value = variableKeeper.getValue(placeHolder)
                }
                rawString =
                    rawString.replace(placeHolder, value.toString())
            }

            svg = SVG.getFromString(rawString)

            svg.setDocumentWidth("100%")
            svg.setDocumentHeight("100%")
            scaffoldView.setSVG(svg)
            scaffoldView.setSVGSource(overlayEntity.svgData!!.svgString!!)


        } catch (e: Exception) {
            Log.w("OverlayViewHelper", "Exception => ".plus(e.message))
        }

        return scaffoldView

    }
}