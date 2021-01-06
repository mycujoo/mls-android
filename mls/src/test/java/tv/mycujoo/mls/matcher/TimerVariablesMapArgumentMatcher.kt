package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.mls.manager.TimerVariable
import java.util.*

class TimerVariablesMapArgumentMatcher(
    private val variableName: String,
    private val value: String
) :
    ArgumentMatcher<HashMap<String, TimerVariable>> {
    override fun matches(argument: HashMap<String, TimerVariable>?): Boolean {
        return argument!!.containsKey(variableName) && argument[variableName]?.getTime() == value
    }
}