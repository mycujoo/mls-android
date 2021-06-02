package tv.mycujoo.mcls.ima

/**
 * Callback on Ads events
 */
interface ImaEventListener {
    /**
     * on Ad started to play
     */
    fun onAdStarted()
    /**
     * on Ad paused by user
     */
    fun onAdPaused()
    /**
     * on Ad resumed by user
     */
    fun onAdResumed()
    /**
     * on Ad completed it's play
     */
    fun onAdCompleted()
}