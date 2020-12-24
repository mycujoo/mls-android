package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.domain.entity.Action

class ShowOverlayActionArgumentMatcher(private val id: String) :
    ArgumentMatcher<Action.ShowOverlayAction> {
    override fun matches(argument: Action.ShowOverlayAction?): Boolean {
        return argument?.id == id
    }
}