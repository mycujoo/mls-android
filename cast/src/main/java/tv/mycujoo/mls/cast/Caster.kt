package tv.mycujoo.mls.cast

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice.CONNECTED
import android.view.ViewStub
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.CastState.*

import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import tv.mycujoo.mls.cast.helper.CustomDataBuilder
import tv.mycujoo.mls.cast.helper.MediaInfoBuilder


class Caster(
    miniControllerViewStub: ViewStub? = null,
    private val mediaRouteButton: MediaRouteButton? = null
) :
    ICaster {
    private lateinit var castContextProvider: ICastContextProvider
    private lateinit var castContext: CastContext

    private var casterSession = CasterSession()

    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>
    private lateinit var castListener: ICastListener

    constructor(
        castContextProvider: ICastContextProvider
    ) : this() {
        this.castContextProvider = castContextProvider
    }

    init {
        inflateMiniController(miniControllerViewStub)
        initMediaRouteButton()
    }

    private fun inflateMiniController(miniControllerViewStub: ViewStub?) {
        miniControllerViewStub?.let {
            it.layoutResource = R.layout.view_cast_mini_controller
            it.inflate()
        }
    }

    private fun initMediaRouteButton() {
        mediaRouteButton?.let {
            CastButtonFactory.setUpMediaRouteButton(it.context, it)
        }
    }

    override fun initialize(
        context: Context,
        castListener: ICastListener
    ): ISessionManagerListener {
        castContext = if (this::castContextProvider.isInitialized) {
            castContextProvider.getCastContext()
        } else {
            CastContextProvider(context).getCastContext()
        }
        casterSession.castSession = castContext.sessionManager.currentCastSession

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

        val sessionManagerWrapper = initSessionManagerWrapper(castListener)
        sessionManagerListener = sessionManagerWrapper.listener
        return sessionManagerWrapper.sessionManagerListener
    }

    private fun initSessionManagerWrapper(castListener: ICastListener): SessionManagerWrapper {

        val progressListener =
            RemoteMediaClient.ProgressListener { progressMs, durationMs ->
                castListener.onRemoteProgressUpdate(progressMs, durationMs)
                getRemoteMediaClient()?.let {
                    castListener.onRemotePlayStatusUpdate(it.isPlaying, it.isBuffering)
                    castListener.onRemoteLiveStatusUpdate(it.isLiveStream)
                }

            }

        this.castListener = castListener
        val localManager = object : ISessionManagerListener {
            private val UPDATE_INTERVAL: Long = 500L

            override fun onSessionStarted(session: ICasterSession?, sessionId: String?) {
                castListener.onConnected(casterSession)
                casterSession.castSession?.remoteMediaClient?.addProgressListener(
                    progressListener,
                    UPDATE_INTERVAL
                )
            }

            override fun onSessionStartFailed(session: ICasterSession?, error: Int) {
                castListener.onDisconnected(casterSession)
            }

            override fun onSessionResumed(session: ICasterSession?, wasSuspended: Boolean) {
                castListener.onConnected(casterSession)
            }

            override fun onSessionResumeFailed(session: ICasterSession?, error: Int) {
                castListener.onDisconnected(casterSession)

            }

            override fun onSessionEnding(session: ICasterSession?) {
                castListener.onDisconnecting(casterSession)

            }

            override fun onSessionEnded(session: ICasterSession?, error: Int) {
                castListener.onDisconnected(casterSession)

            }
        }

        return SessionManagerWrapper(localManager, casterSession)
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

    override fun seekTo(position: Long) {
        val mediaSeekOptions = MediaSeekOptions.Builder().setPosition(position).build()
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
        return casterSession.castSession?.remoteMediaClient
    }

    override fun onResume() {
        if (this::sessionManagerListener.isInitialized.not()) {
            return
        }
        castContext.sessionManager.addSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )

        if (casterSession.castSession != null && casterSession.castSession!!.isConnected) {
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