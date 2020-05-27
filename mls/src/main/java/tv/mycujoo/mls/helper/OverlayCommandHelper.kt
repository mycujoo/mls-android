package tv.mycujoo.mls.helper

import androidx.core.view.children
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.widgets.OverlayHost

class OverlayCommandHelper {
    companion object {
        fun executeInFuture(host: OverlayHost, commandAction: CommandAction) {
//            host.postDelayed({host.children.forEach { view -> if (view.tag == commandAction.targetViewId) }}, commandAction.offset)
        }

    }
}