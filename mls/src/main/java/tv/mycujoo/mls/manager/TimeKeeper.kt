package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.mls.widgets.CreateTimerEntity

class TimeKeeper(private val dispatcher: CoroutineScope) {

    private val timerRelayList = ArrayList<TimerRelay>()

    fun createTimer(createTimerEntity: CreateTimerEntity) {
        val timerRelay = TimerRelay(
            TimerCore(
                createTimerEntity.name,
                createTimerEntity.offset,
                createTimerEntity.format,
                createTimerEntity.direction,
                createTimerEntity.startValue,
                createTimerEntity.step,
                createTimerEntity.capValue
            ),
            BehaviorRelay.createDefault(createTimerEntity.startValue.toString())
        )

        timerRelayList.add(timerRelay)
    }

    fun observe(timerName: String, callback: (Pair<String, String>) -> Unit) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == timerName }
                ?.let { variableRelay ->
                    variableRelay.timerValue.subscribe {
                        callback.invoke(Pair(timerName, variableRelay.timerCore.getFormattedTime()))
                    }
                }
        }
    }

    fun startTimer(name: String) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == name }?.let { timerRelay ->
                timerRelay.timerCore.start(timerRelay.timerValue, dispatcher)
            }
        }

    }

    fun resumeTimer(name: String) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == name }?.timerCore?.resume()
        }
    }

    fun pauseTimer(name: String) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == name }?.timerCore?.pause()
        }
    }

    fun adjustTime(name: String, time: Long) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == name }?.let { timerRelay ->
                timerRelay.timerCore.adjustTime(time, timerRelay.timerValue, dispatcher)
            }
        }
    }

    fun getValue(name: String): String {
        return timerRelayList.firstOrNull { it.timerCore.name == name }?.let { timerRelay ->
            timerRelay.timerCore.getFormattedTime()
        } ?: ""
    }


}