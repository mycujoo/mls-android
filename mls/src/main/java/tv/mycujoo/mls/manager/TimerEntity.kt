package tv.mycujoo.mls.manager

sealed class TimerEntity {
    abstract val name: String
    abstract val offset: Long
    abstract val priority: Int

    data class StartTimer(
        override val name: String,
        override val offset: Long
    ) : TimerEntity() {
        override val priority = 10
    }

    data class PauseTimer(
        override val name: String,
        override val offset: Long
    ) : TimerEntity() {
        override val priority = 5
    }

    data class AdjustTimer(
        override var name: String,
        override val offset: Long,
        val value: Long
    ) : TimerEntity() {
        override val priority = 3
    }

    data class SkipTimer(
        override var name: String,
        override val offset: Long,
        val value: Long
    ) : TimerEntity() {
        override val priority = 2
    }
}