package tv.mycujoo.mls.manager.contracts

import tv.mycujoo.mls.entity.AdjustTimerEntity
import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.entity.PauseTimerEntity
import tv.mycujoo.mls.entity.StartTimerEntity
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
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
            currentTime = (passedTimeFromAdjust / ONE_SECOND_IN_MS) * createTimerEntity.step
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
                    currentTime = (dif / ONE_SECOND_IN_MS) * createTimerEntity.step
                }
                ScreenTimerDirection.DOWN -> {
                    currentTime = now - startValue
                    val dif = currentTime - startTimerEntity.offset
                    currentTime = (dif / ONE_SECOND_IN_MS) * createTimerEntity.step
                }
            }

            return currentTime.toString()
        }

        fun pause(
            pauseTimerEntity: PauseTimerEntity,
            pair: MutablePair<CreateTimerEntity, String>,
            now: Long
        ): String {
            return pauseTimerEntity.offset.toString()
        }
    }

}