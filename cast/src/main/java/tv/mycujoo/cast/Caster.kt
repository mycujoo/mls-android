package tv.mycujoo.cast

import android.app.Activity
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

class Caster(val activity: Activity) : ICaster {
    private var castContext: CastContext = CastContext.getSharedInstance(activity)
    private var castSession: CastSession? = null
    private var sessionManagerListener = castListener()

    override fun initialize() {
        castSession = castContext.sessionManager.currentCastSession
    }

    private fun castListener(): SessionManagerListener<CastSession> {
        return object : SessionManagerListener<CastSession> {
            override fun onSessionStarting(session: CastSession?) {
            }

            override fun onSessionStarted(session: CastSession?, sessionId: String?) {
                onApplicationConnected(session);
            }

            override fun onSessionStartFailed(session: CastSession?, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionResuming(session: CastSession?, sessionId: String?) {
            }

            override fun onSessionResumed(session: CastSession?, wasSuspended: Boolean) {
                onApplicationConnected(session)
            }


            override fun onSessionResumeFailed(session: CastSession?, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionSuspended(session: CastSession?, reason: Int) {
            }

            override fun onSessionEnding(session: CastSession?) {
            }

            override fun onSessionEnded(session: CastSession?, error: Int) {
                onApplicationDisconnected()
            }

            private fun onApplicationConnected(theSession: CastSession?) {
                requireNotNull(theSession)
                castSession = theSession
            }


            private fun onApplicationDisconnected() {
//                updatePlaybackLocation(LOCAL)
//                playbackState = PlaybackState.IDLE
            }

        }
    }

    override fun onResume() {
        castContext.sessionManager.addSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )
    }

    override fun onPause() {
        castContext.sessionManager.removeSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )
    }
}