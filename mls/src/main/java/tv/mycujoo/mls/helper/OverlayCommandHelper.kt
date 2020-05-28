package tv.mycujoo.mls.helper

import android.view.View
import androidx.core.view.children
import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.widgets.OverlayHost

class OverlayCommandHelper {
    companion object {

        fun executeInFuture(
            host: OverlayHost,
            commandAction: CommandAction,
            viewIdentifier: Int?
        ) {
            host.postDelayed({
                host.children.forEach { view ->
                    if (view.tag == viewIdentifier) {
                        view.visibility = View.GONE
                    }
                }
            }, commandAction.offset)
        }

    }
}