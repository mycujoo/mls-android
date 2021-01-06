package tv.mycujoo.mls.caster

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.view.ViewStub
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState.*
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import tv.mycujoo.mls.caster.helper.CustomDataBuilder
import tv.mycujoo.mls.caster.helper.MediaInfoBuilder


class Caster(miniControllerViewStub: ViewStub? = null) : ICaster {
    private lateinit var castContextProvider: ICastContextProvider
    private lateinit var castContext: CastContext
    private var castSession: CastSession? = null
    private var casterSession = CasterSession()
    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>
    private lateinit var castListener: ICastListener

    constructor(
        castContextProvider: ICastContextProvider,
        miniControllerViewStub: ViewStub? = null
    ) : this(miniControllerViewStub) {
        this.castContextProvider = castContextProvider
    }

    init {
        inflateMiniController(miniControllerViewStub)
    }

    private fun inflateMiniController(miniControllerViewStub: ViewStub?) {
        miniControllerViewStub?.let {
            it.layoutResource = R.layout.view_cast_mini_controller
            it.inflate()
        }
    }

    override fun initialize(
        context: Context,
        castListener: ICastListener
    ): SessionManagerListener<CastSession> {
        castContext = if (this::castContextProvider.isInitialized) {
            castContextProvider.getCastContext()
        } else {
            CastContextProvider(context).getCastContext()
        }
        castSession = castContext.sessionManager.currentCastSession
        casterSession.castSession = castSession

        castContext.addCastStateListener { state ->
            when (state) {
                NO_DEVICES_AVAILABLE -> {
                    castListener.onCastStateUpdated(false)
                }
                NOT_CONNECTED,
                CONNECTING,
                CONNECTED -> {
                    castListener.onCastStateUpdated(true)
                }
                else -> {
                    castListener.onCastStateUpdated(false)
                }
            }
        }

        sessionManagerListener = initSessionManagerListener(castListener)
        return sessionManagerListener
    }

    private fun initSessionManagerListener(castListener: ICastListener): SessionManagerListener<CastSession> {
        fun setCastSession(session: CastSession?) {
            castSession = session
            casterSession.castSession = castSession
        }

        val progressListener =
            RemoteMediaClient.ProgressListener { progressMs, durationMs ->
                castListener.onRemoteProgressUpdate(progressMs, durationMs)
                getRemoteMediaClient()?.let {
                    castListener.onRemotePlayStatusUpdate(it.isPlaying, it.isBuffering)
                    castListener.onRemoteLiveStatusUpdate(it.isLiveStream)
                }

            }

        this.castListener = castListener
        return object : SessionManagerListener<CastSession> {
            private val UPDATE_INTERVAL: Long = 500L
            override fun onSessionStarting(session: CastSession?) {
                setCastSession(session)
            }

            override fun onSessionStarted(session: CastSession?, sessionId: String?) {
                setCastSession(session)

                castListener.onConnected(casterSession)
                castSession?.remoteMediaClient?.addProgressListener(
                    progressListener,
                    UPDATE_INTERVAL
                )
            }

            override fun onSessionStartFailed(session: CastSession?, error: Int) {
                setCastSession(session)
                castListener.onDisconnected(casterSession)
            }

            override fun onSessionResuming(session: CastSession?, sessionId: String?) {
                setCastSession(session)
            }

            override fun onSessionResumed(session: CastSession?, wasSuspended: Boolean) {
                setCastSession(session)
                castListener.onConnected(casterSession)
            }

            override fun onSessionResumeFailed(session: CastSession?, error: Int) {
                setCastSession(session)
                castListener.onDisconnected(casterSession)
            }

            override fun onSessionSuspended(session: CastSession?, reason: Int) {
                setCastSession(session)
            }

            override fun onSessionEnding(session: CastSession?) {
                setCastSession(session)
                castListener.onDisconnecting(casterSession)

            }

            override fun onSessionEnded(session: CastSession?, error: Int) {
                setCastSession(session)
                castListener.onDisconnected(casterSession)
            }
        }
    }

    override fun loadRemoteMedia(
        params: CasterLoadRemoteMediaParams
    ) {
        val customData =
            CustomDataBuilder.build(
                params.id,
                params.publicKey,
                params.uuid,
                params.widevine
            )
        val mediaInfo =
            MediaInfoBuilder.build(
                params.fullUrl,
                params.title,
                params.thumbnailUrl,
                customData
            )
        val mediaLoadOptions: MediaLoadOptions =
            MediaLoadOptions.Builder().setAutoplay(params.isPlaying)
                .setPlayPosition(params.currentPosition)
                .build()
        getRemoteMediaClient()?.load(mediaInfo, mediaLoadOptions)
    }

    override fun play() {
        getRemoteMediaClient()?.play()
    }

    override fun pause() {
        getRemoteMediaClient()?.pause()
    }

    override fun seek(mediaSeekOptions: MediaSeekOptions?) {
        getRemoteMediaClient()?.seek(mediaSeekOptions)
    }

    override fun fastForward(amount: Long) {
        getRemoteMediaClient()?.let {
            val newPosition = it.approximateStreamPosition + amount
            val mediaSeekOptions =
                MediaSeekOptions.Builder().setPosition(newPosition).build()
            it.seek(mediaSeekOptions)
        }
    }

    override fun rewind(amount: Long) {
        getRemoteMediaClient()?.let {
            val newPosition = kotlin.math.max(it.approximateStreamPosition + amount, 0L)
            val mediaSeekOptions =
                MediaSeekOptions.Builder().setPosition(newPosition).build()
            it.seek(mediaSeekOptions)
        }
    }


    private fun getRemoteMediaClient(): RemoteMediaClient? {
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