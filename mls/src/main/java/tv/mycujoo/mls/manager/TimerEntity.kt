package tv.mycujoo.mls.manager

sealed class TimerEntity {
    data class StartTimer(
        val name: String,
        val offset: Long
    ) : TimerEntity()

    data class PauseTimer(
        val name: String,
        val offset: Long
    ) : TimerEntity()

    data class AdjustTimer(
        var name: String,
        var offset: Long,
        val value: Long
    ) : TimerEntity()

    data class SkipTimer(
        var name: String,
        var offset: Long,
        val value: Long
    ) : TimerEntity()
}