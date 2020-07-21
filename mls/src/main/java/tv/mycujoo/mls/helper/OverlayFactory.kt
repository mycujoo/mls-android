package tv.mycujoo.mls.helper

import android.content.Context
import android.view.View
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory {

    companion object {
        fun createScaffoldView(
            context: Context,
            overlayObject: OverlayObject,
            variableTranslator: VariableTranslator
        ): ScaffoldView {

            val size = overlayObject.viewSpec.size!!

            val scaffoldView = ScaffoldView(size.first, size.second, context)
            scaffoldView.id = View.generateViewId()
            scaffoldView.tag = overlayObject.id

            overlayObject.variablePlaceHolders.forEach { entry ->
                // VALUE of place-holder, is the KEY in the set_variable map
                variableTranslator.createVariableLiveEventIfNotExisted(entry.value)
                variableTranslator.observe(
                    entry.value
                ) { scaffoldView.onVariableUpdated(variableTranslator) }
            }

            return scaffoldView

        }

    }
}