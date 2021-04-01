package tv.mycujoo.mcls.ima

interface ImaEventListener {
    fun onAdStarted()
    fun onAdPaused()
    fun onAdResumed()
    fun onAdCompleted()
}