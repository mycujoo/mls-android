package tv.mycujoo.mls.ima

interface ImaEventListener {
    fun onAdStarted()
    fun onAdPaused()
    fun onAdResumed()
    fun onAdCompleted()
}