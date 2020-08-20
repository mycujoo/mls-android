package tv.mycujoo.fake

import android.animation.ObjectAnimator
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.mls.manager.TimeKeeper
import tv.mycujoo.mls.manager.VariableTranslator
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.widgets.ScaffoldView

class FakeViewHandler(coroutineScope: CoroutineScope, private val idlingResource: CountingIdlingResource) : IViewHandler {


    var lastRemovedAnimationId : String? = null

    private val variableTranslator = VariableTranslator(coroutineScope)
    private val timeKeeper = TimeKeeper(coroutineScope)

    override fun addAnimation(id: String, objectAnimator: ObjectAnimator) {
    }

    override fun removeAnimation(id: String) {
        lastRemovedAnimationId = id
    }

    override fun getAnimations(): List<ObjectAnimator> {
        return emptyList()
    }

    override fun getAnimationWithTag(id: String): ObjectAnimator? {
        return null
    }

    override fun attachOverlayView(view: ScaffoldView) {
    }

    override fun detachOverlayView(view: ScaffoldView?) {
    }

    override fun getOverlayView(id: String): ScaffoldView? {
        return null
    }

    override fun overlayBlueprintIsNotAttached(id: String): Boolean {
        return false
    }

    override fun overlayBlueprintIsAttached(id: String): Boolean {
        return false
    }

    override fun clearAll() {
    }

    override fun incrementIdlingResource() {
        idlingResource.increment()
    }


    override fun decrementIdlingResource() {
        if (idlingResource.isIdleNow.not()) {
            idlingResource.decrement()
        }
    }

    override fun getVariableTranslator(): VariableTranslator {
        return variableTranslator
    }

    override fun getTimeKeeper(): TimeKeeper {
        return timeKeeper
    }
}