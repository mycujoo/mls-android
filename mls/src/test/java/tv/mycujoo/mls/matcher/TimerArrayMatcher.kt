package tv.mycujoo.mls.matcher

import org.mockito.ArgumentMatcher
import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.model.MutablePair

class TimerArrayMatcher(private val array: ArrayList<Set<MutablePair<CreateTimerEntity, String>>>) :
    ArgumentMatcher<ArrayList<Set<MutablePair<CreateTimerEntity, String>>>> {
    lateinit var argument: ArrayList<Set<MutablePair<CreateTimerEntity, String>>>
    override fun matches(argument: ArrayList<Set<MutablePair<CreateTimerEntity, String>>>?): Boolean {
        this.argument = argument!!
        return argument.first().first().first.name == array[0].first().first.name &&
                argument.first().first().second == array[0].first().second
    }

    override fun toString(): String {
        return super.toString().plus(" actual value -> ${this.argument.first().first().first} , ${this.argument.first().first().second}")
    }
}