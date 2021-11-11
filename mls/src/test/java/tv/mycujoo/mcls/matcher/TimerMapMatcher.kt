package tv.mycujoo.mcls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.mcls.manager.TimerVariable

class TimerMapMatcher(private val hashMap: HashMap<String, TimerVariable>) :
    ArgumentMatcher<HashMap<String, TimerVariable>> {

    lateinit var argument: HashMap<String, TimerVariable>

    override fun matches(argument: HashMap<String, TimerVariable>?): Boolean {
        this.argument = argument!!
        return argument.keys.all { key ->
            argument[key]!!.name == hashMap[key]!!.name &&
            argument[key]!!.getTime() == hashMap[key]!!.getTime()
        }
    }


    override fun toString(): String {
        return super.toString().plus(
            " actual value -> ${this.argument}"
        )
    }
}