package tv.mycujoo.mcls.cast

interface ISessionManagerListener {
    fun onSessionStarted(session: ICasterSession?, sessionId: String?)

    fun onSessionStartFailed(session: ICasterSession?, error: Int)

    fun onSessionResumed(session: ICasterSession?, wasSuspended: Boolean)

    fun onSessionResumeFailed(session: ICasterSession?, error: Int)

    fun onSessionEnding(session: ICasterSession?)

    fun onSessionEnded(session: ICasterSession?, error: Int)
}
