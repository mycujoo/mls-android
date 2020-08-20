package tv.mycujoo.fake

import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.widgets.ScaffoldView

class FakeViewHandler(
    coroutineScope: CoroutineScope,
    idlingResource: CountingIdlingResource
) : ViewHandler(coroutineScope, idlingResource) {

    var lastRemovedAnimationId: String? = null
    var lastRemovedView: ScaffoldView? = null
    var lastAddedView: ScaffoldView? = null

    override fun removeAnimation(id: String) {
        lastRemovedAnimationId = id
        super.removeAnimation(id)
    }

    override fun attachOverlayView(view: ScaffoldView) {
        lastAddedView = view
        super.attachOverlayView(view)
    }

    override fun detachOverlayView(view: ScaffoldView?) {
        lastRemovedView = view
        super.detachOverlayView(view)
    }
}