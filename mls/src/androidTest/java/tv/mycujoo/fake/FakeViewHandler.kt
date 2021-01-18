package tv.mycujoo.fake

import androidx.test.espresso.idling.CountingIdlingResource
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.widgets.ScaffoldView

class FakeViewHandler(
    idlingResource: CountingIdlingResource
) : ViewHandler(idlingResource) {

    var lastRemovedAnimationId: String? = null
    var lastRemovedView: ScaffoldView? = null
    var lastAddedView: ScaffoldView? = null

    override fun removeAnimation(overlayTag: String) {
        lastRemovedAnimationId = overlayTag
        super.removeAnimation(overlayTag)
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