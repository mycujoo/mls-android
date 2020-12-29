package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.domain.entity.VariableEntity
import java.util.*

class VariablesMapArgumentMatcher(private val variableName: String, private val value: String) :
    ArgumentMatcher<HashMap<String, VariableEntity>> {
    override fun matches(argument: HashMap<String, VariableEntity>?): Boolean {
        return argument!!.containsKey(variableName) && argument[variableName]?.variable?.printValue() == value
    }
}