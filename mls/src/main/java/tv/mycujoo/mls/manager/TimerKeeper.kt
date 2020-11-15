package tv.mycujoo.mls.manager

import com.jakewharton.rxrelay3.BehaviorRelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tv.mycujoo.mls.entity.*

class TimerKeeper(private val dispatcher: CoroutineScope) : ITimerKeeper {

    private val timerRelayList = ArrayList<TimerTwin>()
    private val timerPublisherMap = HashMap<String, BehaviorRelay<String>>()

    override fun getTimerRelayList(): List<TimerTwin> {
        return timerRelayList
    }

    override fun getTimerNames(): List<String> {
        return timerPublisherMap.keys.toList()
    }

    fun createTimerPublisher(name: String) {
        if (timerPublisherMap.containsKey(name)) {
            return
        }
        timerPublisherMap[name] = (BehaviorRelay.createDefault(""))
    }

    override fun observe(timerName: String, callback: (Pair<String, String>) -> Unit) {
        dispatcher.launch {
            timerPublisherMap[timerName]?.let { behaviorRelay ->
                behaviorRelay.subscribe {
                    callback.invoke(Pair(timerName, it))
                }
            }
        }

    }

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


    override fun getValue(name: String): String {
        return timerPublisherMap[name]?.value ?: ""
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

    fun pauseTimer(pauseTimerEntity: PauseTimerEntity, currentTime: Long) {
        timerRelayList.firstOrNull { it.timerCore.name == pauseTimerEntity.name }
            ?.let { timerTwin ->
                timerTwin.timerCore.setTime(pauseTimerEntity, currentTime)
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

    override fun notify(timerVariables: HashMap<String, TimerVariable>) {
        timerPublisherMap.forEach { e ->
            timerVariables[e.key]?.let { timerVariable ->
                e.value.accept(timerVariable.getTime())
            }
        }

    }
}