package tv.mycujoo.mls.manager

import tv.mycujoo.mls.manager.contracts.ITimerProcessor
import tv.mycujoo.mls.widgets.*

class TimerProcessor(
    private val timerCollection: List<TimerCollection>,
    private val timerKeeper: TimerKeeper,
    private val appliedCreateTimer: ArrayList<String>
) : ITimerProcessor {
    override fun process(
        currentTime: Long
    ): Set<String> {

        val toBeNotified = mutableSetOf<String>()

        timerCollection.forEach { timerEntity ->


            timerEntity.getAllActionsUntil(currentTime).forEach { action ->

                when (action) {
                    is TimerEntity.CreateTimer -> {
                        createTimer(action.createTimerEntity)
                        toBeNotified.add(action.createTimerEntity.name)
                    }
                    is TimerEntity.StartTimer -> {
                        startTimer(action.startTimerEntity, currentTime)
                        toBeNotified.add(action.startTimerEntity.name)
                    }
                    is TimerEntity.PauseTimer -> {
//                        pauseTimer(action.pauseTimerEntity)
//                        toBeNotified.add(action.pauseTimerEntity.name)
                    }
                    is TimerEntity.AdjustTimer -> {
                        adjustTimer(action.adjustTimerEntity, currentTime)
                        toBeNotified.add(action.adjustTimerEntity.name)

                    }
                    is TimerEntity.SkipTimer -> {
                        skipTimer(action.skipTimerEntity)
                        toBeNotified.add(action.skipTimerEntity.name)

                    }
                    is TimerEntity.KillTimer -> {
//                        clearTimer(action.timerName)
                    }
                }


            }
        }
        return toBeNotified

    }

    private fun createTimer(createTimerEntity: CreateTimerEntity) {
        appliedCreateTimer.add(createTimerEntity.name)
        timerKeeper.createTimer(createTimerEntity)
    }


    private fun startTimer(
        startTimerEntity: StartTimerEntity,
        currentTime: Long
    ) {
        timerKeeper.startTimer(startTimerEntity, currentTime)
    }

    private fun adjustTimer(adjustTimerEntity: AdjustTimerEntity, currentTime: Long) {
        timerKeeper.adjustTimer(adjustTimerEntity, currentTime)
    }

    private fun skipTimer(skipTimerEntity: SkipTimerEntity) {
        timerKeeper.skipTimer(skipTimerEntity)
    }


    private fun clearTimer(timerName: String) {
        timerKeeper.killTimer(timerName)
        appliedCreateTimer.remove(timerName)
    }


}