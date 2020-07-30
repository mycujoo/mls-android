package tv.mycujoo.mls.manager

import android.util.Log
import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.mls.widgets.AdjustTimerEntity
import tv.mycujoo.mls.widgets.CreateTimerEntity
import tv.mycujoo.mls.widgets.SkipTimerEntity
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
                ?.let { timerTwin ->
                    timerTwin.timerRelay.subscribe {
                        callback.invoke(Pair(timerName, timerTwin.timerCore.getFormattedTime()))
                    }
                }
        }
    }


    fun getValue(name: String): String {
        return timerRelayList.firstOrNull { it.timerCore.name == name }?.timerCore?.getFormattedTime()
            ?: ""
    }

    /**region Using commands [Entities]*/
    fun startTimer(
        startTimerEntity: StartTimerEntity,
        currentTime: Long
    ) {
        timerRelayList.firstOrNull { it.timerCore.name == startTimerEntity.name }
            ?.let { timerTwin ->
                timerTwin.timerCore.setTime(startTimerEntity, currentTime)
            }

    }


    fun adjustTimer(adjustTimerEntity: AdjustTimerEntity, currentTime: Long) {
        timerRelayList.firstOrNull { it.timerCore.name == adjustTimerEntity.name }
            ?.let { timerTwin ->
                timerTwin.timerCore.setTime(adjustTimerEntity, currentTime)
            }
    }

    fun skipTimer(skipTimerEntity: SkipTimerEntity) {
        timerRelayList.firstOrNull { it.timerCore.name == skipTimerEntity.name }
            ?.let { timerTwin ->
                timerTwin.timerCore.setTime(skipTimerEntity)
            }
    }


    fun killTimer(timerName: String) {
        Log.d("TimeKeeper", "killTimer() for $timerName")
        dispatcher.launch {
            timerRelayList.firstOrNull { it.timerCore.name == timerName }?.let { timerTwin ->
                timerTwin.timerCore.kill()
            }
        }
    }

    fun notify(timerName: String) {
        timerRelayList.firstOrNull { it.timerCore.name == timerName }
            ?.let { timerRelay ->
                timerRelay.timerCore.notifyObservers(timerRelay)
            }
    }


}