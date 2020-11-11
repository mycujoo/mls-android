package tv.mycujoo.mls.manager.contracts

import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity
import tv.mycujoo.mls.model.MutablePair
import tv.mycujoo.mls.model.ScreenTimerDirection

class TimerHelper {
    companion object {
        fun adjust(
            adjustTimerEntity: AdjustTimerEntity,
            pair: MutablePair<CreateTimerEntity, String>,
            now: Long
        ): String {

            var currentTime: Long
            val createTimerEntity = pair.first

            val passedTimeFromAdjust = now - adjustTimerEntity.offset
            currentTime = (passedTimeFromAdjust / 1000L) * createTimerEntity.step
            currentTime += adjustTimerEntity.value
            return currentTime.toString()
        }

        fun start(
            startTimerEntity: StartTimerEntity,
            pair: MutablePair<CreateTimerEntity, String>,
            now: Long
        ): String {

            var currentTime: Long
            val createTimerEntity = pair.first

            val direction = createTimerEntity.direction
            val startValue = createTimerEntity.startValue

            when (direction) {
                ScreenTimerDirection.UP -> {
                    currentTime = now + startValue
                    val dif = currentTime - startTimerEntity.offset
                    currentTime = (dif / 1000L) * createTimerEntity.step
                }
                ScreenTimerDirection.DOWN -> {
                    currentTime = now - startValue
                    val dif = currentTime - startTimerEntity.offset
                    currentTime = (dif / 1000L) * createTimerEntity.step
                }
            }

            return currentTime.toString()
        }
    }

}