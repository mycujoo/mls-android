package tv.mycujoo.mcls.helper

import android.content.Context
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mcls.manager.IVariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.widgets.ScaffoldView

interface IOverlayFactory {
    fun createScaffoldView(
        context: Context,
        showOverlayAction: Action.ShowOverlayAction,
        variableTranslator: VariableTranslator,
        variableKeeper: IVariableKeeper
    ): ScaffoldView
}