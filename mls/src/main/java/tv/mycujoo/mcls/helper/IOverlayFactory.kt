package tv.mycujoo.mcls.helper

import android.content.Context
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mcls.manager.IVariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.widgets.ScaffoldView

/**
 * Contract for creating ready-to-be-displayed ScaffoldView
 */
interface IOverlayFactory {
    fun createScaffoldView(
        showOverlayAction: Action.ShowOverlayAction,
    ): ScaffoldView
}