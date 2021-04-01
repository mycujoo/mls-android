package tv.mycujoo.mcls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.domain.entity.TransitionSpec

class TransitionSpecArgumentMatcher(private val offset: Long) :
    ArgumentMatcher<TransitionSpec> {
    override fun matches(argument: TransitionSpec?): Boolean {
        return argument?.offset == offset
    }
}