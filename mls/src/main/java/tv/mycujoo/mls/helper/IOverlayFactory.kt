package tv.mycujoo.mls.helper

import android.content.Context
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

interface IOverlayFactory {
    fun createScaffoldView(
        context: Context,
        showOverlayAction: Action.ShowOverlayAction,
        variableTranslator: VariableTranslator,
        variableKeeper: IVariableKeeper
    ): ScaffoldView
}