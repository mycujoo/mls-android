package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.mls.widgets.CreateTimerEntity
import tv.mycujoo.mls.widgets.StartTimerEntity

class TimeKeeper(private val dispatcher: CoroutineScope) {

    val timerRelayList = ArrayList<TimerTwin>()

    fun createTimer(createTimerEntity: CreateTimerEntity) {
        val timerRelay = TimerTwin(
            TimerCore(
                createTimerEntity.name,
                createTimerEntity.offset,
                createTimerEntity.format,
                createTimerEntity.direction,
                createTimerEntity.startValue,
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
                    variableRelay.timerRelay.subscribe {
                        callback.invoke(Pair(timerName, variableRelay.timerCore.getFormattedTime()))
                    }
                }
        }
    }

    fun startTimer(name: String) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == name }?.let { timerRelay ->
                timerRelay.timerCore.start(timerRelay.timerRelay, dispatcher)
            }
        }

    }

    fun adjustTime(name: String, time: Long) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == name }?.let { timerRelay ->
                timerRelay.timerCore.adjustTime(time, timerRelay.timerRelay, dispatcher)
            }
        }
    }

    fun getValue(name: String): String {
        return timerRelayList.firstOrNull { it.timerCore.name == name }?.timerCore?.getFormattedTime()
            ?: ""
    }

    /**
     * calculates difference between current time and given StartTimerEntity,
     *
     */
    fun tuneWithStartEntity(
        timerName: String,
        startTimerEntity: StartTimerEntity,
        currentTime: Long
    ) {
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == timerName }?.let { timerRelay ->
                timerRelay.timerCore.tuneWithStartEntity(
                    currentTime,
                    startTimerEntity,
                    timerRelay.timerRelay,
                    dispatcher
                )
            }
        }


    }

}