package tv.mycujoo.mls.manager.contracts

interface ITimerProcessor {
    fun process(
        currentTime: Long
    ): Set<String>
}