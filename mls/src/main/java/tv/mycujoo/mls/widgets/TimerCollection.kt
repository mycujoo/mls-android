package tv.mycujoo.mls.widgets

import tv.mycujoo.mls.entity.*
import tv.mycujoo.mls.helper.TimeRangeHelper.Companion.isOffsetUntilNow
import tv.mycujoo.mls.manager.TimerEntity

class TimerCollection(
    val name: String,
    val createCommand: CreateTimerEntity,
    val startCommand: ArrayList<StartTimerEntity> = ArrayList(),
    val pauseCommand: ArrayList<PauseTimerEntity> = ArrayList(),
    val adjustCommand: ArrayList<AdjustTimerEntity> = ArrayList(),
    val skipCommand: ArrayList<SkipTimerEntity> = ArrayList()
) {

    fun getAllActionsUntil(currentTime: Long): List<TimerEntity> {
        val listOfTimerActions = ArrayList<TimerEntity>()

        if (isOffsetUntilNow(currentTime, createCommand.offset)) {
            listOfTimerActions.add(TimerEntity.CreateTimer(createCommand))
        } else {
            listOfTimerActions.add(TimerEntity.KillTimer(createCommand.name))
        }

        startCommand.forEach {
            if (isOffsetUntilNow(currentTime, it.offset)) {
                listOfTimerActions.add(TimerEntity.StartTimer(it))
            }
        }

        pauseCommand.forEach {
            if (isOffsetUntilNow(currentTime, it.offset)) {
                listOfTimerActions.add(TimerEntity.PauseTimer(it))
            }
        }

        adjustCommand.forEach {
            if (isOffsetUntilNow(currentTime, it.offset)) {
                listOfTimerActions.add(TimerEntity.AdjustTimer(it))
            }
        }

        skipCommand.forEach {
            if (isOffsetUntilNow(currentTime, it.offset)) {
                listOfTimerActions.add(TimerEntity.SkipTimer(it))
            }
        }


        return listOfTimerActions

    }


}