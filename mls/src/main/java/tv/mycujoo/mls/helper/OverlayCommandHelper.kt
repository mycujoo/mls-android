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
                    if (view.id == viewIdentifier) {
                        if (commandAction.verb.equals("remove", true)) {
                            host.removeView(view)
                        } else if (commandAction.verb.equals("hide", true)) {
                            view.visibility = View.INVISIBLE
                        } else if (commandAction.verb.equals("show", true)) {
                            view.visibility = View.VISIBLE
                        }
                    }
                }
            }, commandAction.offset)
        }

    }
}