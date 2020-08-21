package tv.mycujoo.mls.manager

interface ITimerKeeper {

    fun observe(timerName: String, callback: (Pair<String, String>) -> Unit)
    fun getValue(name: String): String
}
