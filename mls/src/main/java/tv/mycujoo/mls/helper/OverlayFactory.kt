package tv.mycujoo.mls.helper

import android.content.Context
import android.view.View
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.mls.manager.TimeKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

class OverlayFactory {

    companion object {
        //todo [WIP]
        fun createScaffoldView(
            context: Context,
            overlayObject: OverlayObject,
            variableTranslator: VariableTranslator,
            timeKeeper: TimeKeeper
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
                ) { scaffoldView.onVariableUpdated(it) }
            }

            overlayObject.variablePlaceHolders.forEach { entry ->
                timeKeeper.observe(entry.value) { scaffoldView.onVariableUpdated(it) }
            }

            return scaffoldView

        }

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

            overlayEntity.variablePlaceHolders.forEach { entry ->
                // VALUE of place-holder, is the KEY in the set_variable map
                variableTranslator.createVariableLiveEventIfNotExisted(entry.value)
                variableTranslator.observe(
                    entry.value
                ) { scaffoldView.onVariableUpdated(it) }
            }

            overlayEntity.variablePlaceHolders.forEach { entry ->
                timeKeeper.observe(entry.value) { scaffoldView.onVariableUpdated(it) }
            }

            return scaffoldView

        }

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
                ) { scaffoldView.onVariableUpdated(it) }
            }

            return scaffoldView

        }

    }
}