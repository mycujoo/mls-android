package tv.mycujoo.mls.cast

import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

class SessionManagerWrapper(
    val sessionManagerListener: ISessionManagerListener,
    val casterSession: CasterSession
) {
    val listener: SessionManagerListener<CastSession>

    init {
        listener =
            object : SessionManagerListener<CastSession> {
                override fun onSessionStarting(session: CastSession?) {
                    setCastSession(session)
                }

                override fun onSessionStarted(session: CastSession?, sessionId: String?) {
                    setCastSession(session)
                    sessionManagerListener.onSessionStarted(casterSession, sessionId)
                }

                override fun onSessionStartFailed(session: CastSession?, error: Int) {
                    setCastSession(session)
                    sessionManagerListener.onSessionStartFailed(casterSession, error)
                }

                override fun onSessionResuming(session: CastSession?, sessionId: String?) {
                    setCastSession(session)
                }

                override fun onSessionResumed(session: CastSession?, wasSuspended: Boolean) {
                    setCastSession(session)
                    sessionManagerListener.onSessionResumed(casterSession, wasSuspended)
                }

                override fun onSessionResumeFailed(session: CastSession?, error: Int) {
                    setCastSession(session)
                    sessionManagerListener.onSessionResumeFailed(casterSession, error)
                }

                override fun onSessionSuspended(session: CastSession?, reason: Int) {
                    setCastSession(session)
                }

                override fun onSessionEnding(session: CastSession?) {
                    setCastSession(session)
                    sessionManagerListener.onSessionEnding(casterSession)
                }

                override fun onSessionEnded(session: CastSession?, error: Int) {
                    setCastSession(session)
                    sessionManagerListener.onSessionEnded(casterSession, error)
                }
            }

    }

    fun setCastSession(session: CastSession?) {
        casterSession.castSession = session
    }
}