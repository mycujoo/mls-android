package tv.mycujoo.cast

import android.view.ViewStub
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient


class Caster(miniControllerViewStub: ViewStub? = null) : ICaster {
    private lateinit var castContext: CastContext
    private var castSession: CastSession? = null
    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>
    private lateinit var castListener: ICastListener

    init {
        inflateMiniController(miniControllerViewStub)

    }

    private fun inflateMiniController(miniControllerViewStub: ViewStub?) {
        miniControllerViewStub?.let {
            it.layoutResource = R.layout.view_cast_mini_controller
            it.inflate()
        }
    }

    override fun initialize(castProvider: ICastContextProvider, castListener: ICastListener) {
        castContext = castProvider.getCastContext()
        castSession = castContext.sessionManager.currentCastSession
        sessionManagerListener = initSessionManagerListener(castListener)
    }

    private fun initSessionManagerListener(castListener: ICastListener): SessionManagerListener<CastSession> {
        val progressListener =
            RemoteMediaClient.ProgressListener { progressMs, durationMs ->
                castListener.onRemoteProgressUpdate(progressMs, durationMs)
                getRemoteMediaClient()?.let {
                    castListener.onRemotePlayStatusUpdate(it.isPlaying, it.isBuffering)
                }

            }

        this.castListener = castListener
        return object : SessionManagerListener<CastSession> {
            override fun onSessionStarting(session: CastSession?) {
                castSession = session
            }

            override fun onSessionStarted(session: CastSession?, sessionId: String?) {
                castSession = session
                castListener.onConnected(session)
                castSession?.remoteMediaClient?.addProgressListener(progressListener, 1000L)
            }

            override fun onSessionStartFailed(session: CastSession?, error: Int) {
                castSession = session
                castListener.onDisconnected(session)
            }

            override fun onSessionResuming(session: CastSession?, sessionId: String?) {
                castSession = session
            }

            override fun onSessionResumed(session: CastSession?, wasSuspended: Boolean) {
                castSession = session
                castListener.onConnected(session)
            }

            override fun onSessionResumeFailed(session: CastSession?, error: Int) {
                castSession = session
                castListener.onDisconnected(session)
            }

            override fun onSessionSuspended(session: CastSession?, reason: Int) {
                castSession = session
            }

            override fun onSessionEnding(session: CastSession?) {
                castSession = session
                castListener.onDisconnecting(session)

            }

            override fun onSessionEnded(session: CastSession?, error: Int) {
                castSession = session
                castListener.onDisconnected(session)
            }
        }
    }

    override fun getRemoteMediaClient(): RemoteMediaClient? {
        return castSession?.remoteMediaClient
    }

    override fun onResume() {
        if (this::sessionManagerListener.isInitialized.not()) {
            return
        }
        castContext.sessionManager.addSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )

        if (castSession != null && castSession!!.isConnected) {
            castListener.onPlaybackLocationUpdated(false)
        } else {
            castListener.onPlaybackLocationUpdated(true)

        }

    }

    override fun onPause() {
        if (this::sessionManagerListener.isInitialized.not()) {
            return
        }
        castContext.sessionManager.removeSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )
    }
}