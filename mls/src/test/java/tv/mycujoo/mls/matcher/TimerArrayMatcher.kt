package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.model.MutablePair

class TimerArrayMatcher(private val hashMap: HashMap<String, MutablePair<CreateTimerEntity, String>>) :
    ArgumentMatcher<HashMap<String, MutablePair<CreateTimerEntity, String>>> {

    lateinit var argument: HashMap<String, MutablePair<CreateTimerEntity, String>>


    override fun matches(argument: HashMap<String, MutablePair<CreateTimerEntity, String>>?): Boolean {
        this.argument = argument!!
        return argument.keys.all { key ->
            argument[key]!!.first.name == hashMap[key]!!.first.name
            argument[key]!!.second == hashMap[key]!!.second
        }
    }


    override fun toString(): String {
        return super.toString().plus(
            " actual value -> ${this.argument}"
        )
    }
}