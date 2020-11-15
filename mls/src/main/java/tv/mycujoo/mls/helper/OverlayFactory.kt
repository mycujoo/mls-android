package tv.mycujoo.mls.helper

import android.content.Context
import android.util.Log
import android.view.View
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.mls.manager.ITimerKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory : IOverlayFactory {

    override fun createScaffoldView(
        context: Context,
        overlayEntity: OverlayEntity,
        variableTranslator: VariableTranslator,
        timerKeeper: ITimerKeeper
    ): ScaffoldView {

        val size = overlayEntity.viewSpec.size!!

        val scaffoldView = ScaffoldView(size.first, size.second, context)
        scaffoldView.id = View.generateViewId()
        scaffoldView.tag = overlayEntity.id

        scaffoldView.setVariablePlaceHolder(overlayEntity.variablePlaceHolders)


        overlayEntity.variablePlaceHolders.forEach { entry ->
            // VALUE of place-holder, is the KEY in the set_variable map
            variableTranslator.createVariableTripleIfNotExisted(entry)
            variableTranslator.observe(
                entry
            ) { scaffoldView.onVariableUpdated(it) }

            variableTranslator.getValue(entry)?.let {
                scaffoldView.initialVariables(Pair(entry, it))
            }

            timerKeeper.getTimerNames().firstOrNull {
                it == entry
            }?.let {
                timerKeeper.observe(entry) { scaffoldView.onVariableUpdated(it) }
                scaffoldView.initialVariables(Pair(entry, timerKeeper.getValue(entry)))
            }
        }

        try {
            val svg: SVG
            var rawString = overlayEntity.svgData!!.svgString!!

            overlayEntity.variablePlaceHolders.forEach { placeHolder ->
                var value = variableTranslator.getValue(placeHolder)
                if (value == null) {
                    value = timerKeeper.getValue(placeHolder)
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