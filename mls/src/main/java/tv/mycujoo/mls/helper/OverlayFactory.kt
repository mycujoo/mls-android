package tv.mycujoo.mls.helper

import android.content.Context
import android.util.Log
import android.view.View
import com.caverock.androidsvg.SVG
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.mls.manager.TimeKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory {

    companion object {
        fun createScaffoldView(
            context: Context,
            overlayEntity: OverlayEntity,
            variableTranslator: VariableTranslator,
            timeKeeper: TimeKeeper
        ): ScaffoldView {

            val size = overlayEntity.viewSpec.size!!

            val scaffoldView = ScaffoldView(size.first, size.second, context)
            scaffoldView.id = View.generateViewId()
            scaffoldView.tag = overlayEntity.id

            scaffoldView.setVariablePlaceHolder(overlayEntity.variablePlaceHolders)


            overlayEntity.variablePlaceHolders.forEach { entry ->
                // VALUE of place-holder, is the KEY in the set_variable map
                variableTranslator.createVariableLiveEventIfNotExisted(entry)
                variableTranslator.observe(
                    entry
                ) { scaffoldView.onVariableUpdated(it) }

                variableTranslator.getValue(entry)?.let {
                    scaffoldView.initialVariables(Pair(entry, it))
                }
            }

            overlayEntity.variablePlaceHolders.forEach { entry ->
                timeKeeper.observe(entry) { scaffoldView.onVariableUpdated(it) }
            }


            try {
                val svg: SVG
                var rawString = overlayEntity.svgData!!.svgString!!

                overlayEntity.variablePlaceHolders.forEach { placeHolder ->
                    val value = variableTranslator.getValue(placeHolder)
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
}