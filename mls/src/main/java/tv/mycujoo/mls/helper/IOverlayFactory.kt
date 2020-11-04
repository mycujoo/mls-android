package tv.mycujoo.mls.helper

import android.content.Context
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.mls.manager.ITimerKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.widgets.ScaffoldView

interface IOverlayFactory {
    fun createScaffoldView(
        context: Context,
        overlayEntity: OverlayEntity,
        variableTranslator: VariableTranslator,
        timerKeeper: ITimerKeeper
    ): ScaffoldView
}