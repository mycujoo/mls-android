package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.domain.entity.Action

class ActionArgumentMatcher(private val id: String) :
    ArgumentMatcher<Action> {
    override fun matches(argument: Action): Boolean {
        return argument.id == id
    }
}