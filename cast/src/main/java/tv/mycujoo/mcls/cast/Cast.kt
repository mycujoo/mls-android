package tv.mycujoo.mcls.cast

import android.content.Context
import android.util.Log
import android.view.ViewStub
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaSeekOptions
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import tv.mycujoo.mcls.cast.helper.CustomDataBuilder
import tv.mycujoo.mcls.cast.helper.MediaInfoBuilder

/**
 * Integration with Google Cast
 * @param miniControllerViewStub view-stub which mini controller will inflate at
 * @property mediaRouteButton button to setup as MediaRouteButton
 * @property receiverAppId web receiverAppId
 */
class Cast(
    miniControllerViewStub: ViewStub? = null,
    private val mediaRouteButton: MediaRouteButton? = null,
    private val receiverAppId: String? = null
) :
    ICast {

    /**region Fields*/
    private lateinit var castContextProvider: ICastContextProvider
    private lateinit var castContext: CastContext

    private var casterSession = CasterSession()

    private lateinit var sessionManagerListener: SessionManagerListener<CastSession>
    private lateinit var castListener: ICastListener

    /**endregion */

    constructor(
        castContextProvider: ICastContextProvider
    ) : this() {
        this.castContextProvider = castContextProvider
    }

    init {
        inflateMiniController(miniControllerViewStub)
        initMediaRouteButton()
    }

    /**
     * Inflate mini-controller at given view-stub
     * @param miniControllerViewStub view-stub which will mini-controller inflate at
     */
    private fun inflateMiniController(miniControllerViewStub: ViewStub?) {
        miniControllerViewStub?.let {
            it.layoutResource = R.layout.view_cast_mini_controller
            it.inflate()
        }
    }

    /**
     * Setup MediaRouteButton
     */
    private fun initMediaRouteButton() {
        mediaRouteButton?.let {
            CastButtonFactory.setUpMediaRouteButton(it.context, it)
        }
    }

    /**
     * Initialize integration by
     * 1. Creating CastContext
     * 2. Setting current session to newly created CastContext
     * 3. Return SessionManagerListener
     *
     * @param context context to create CastContext from
     * @param castListener listener for cast session events
     * @return SessionManagerListener
     * @see ICastListener
     * @see SessionManagerListener
     */
    override fun initialize(
        context: Context,
        castListener: ICastListener
    ): ISessionManagerListener {
        castContext = if (this::castContextProvider.isInitialized) {
            castContextProvider.getCastContext()
        } else {
            CastContextProvider(context).getCastContext()
        }

        //overrides default app-id if provided
        receiverAppId?.let {
            castContext.setReceiverApplicationId(it)
        }

        casterSession.castSession = castContext.sessionManager.currentCastSession

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
                castListener.onSessionStarted(casterSession)
                casterSession.castSession?.remoteMediaClient?.addProgressListener(
                    progressListener,
                    UPDATE_INTERVAL
                )
            }

            override fun onSessionStartFailed(session: ICasterSession?, error: Int) {
                castListener.onSessionStartFailed(casterSession)
            }

            override fun onSessionResumed(session: ICasterSession?, wasSuspended: Boolean) {
                castListener.onSessionResumed(casterSession)
                casterSession.castSession?.remoteMediaClient?.addProgressListener(
                    progressListener,
                    UPDATE_INTERVAL
                )
            }

            override fun onSessionResumeFailed(session: ICasterSession?, error: Int) {
                castListener.onSessionResumeFailed(casterSession)

            }

            override fun onSessionEnding(session: ICasterSession?) {
                castListener.onSessionEnding(casterSession)

            }

            override fun onSessionEnded(session: ICasterSession?, error: Int) {
                castListener.onSessionEnded(casterSession)
            }
        }

        return SessionManagerWrapper(localManager, casterSession)
    }

    /**
     * Load given parameters into remote device
     * @param params CasterLoadRemoteMediaParams data needed to load content on remote client
     */
    override fun loadRemoteMedia(
        params: CasterLoadRemoteMediaParams
    ) {
        Log.d("MLS_CAST", "loadRemoteMedia Starting")
        val customData = params.customPlaylistUrl?.let {
            CustomDataBuilder.build(
                it
            )
        } ?: CustomDataBuilder.build(
            params.id,
            params.publicKey,
            params.pseudoUserId,
            params.identityToken
        )

        Log.d("MLS_CAST", "loadRemoteMedia: $customData")

        val mediaInfo = MediaInfoBuilder.build(
            "",
            params.title,
            params.thumbnailUrl,
            customData
        )

        val mediaLoadOptions: MediaLoadOptions = MediaLoadOptions
            .Builder()
            .setAutoplay(params.isPlaying)
            .setPlayPosition(params.currentPosition)
            .build()
        getRemoteMediaClient()?.load(mediaInfo, mediaLoadOptions)
    }

    /**
     * Play content on remote client
     */
    override fun play() {
        getRemoteMediaClient()?.play()
    }

    /**
     * Pause content on remote client
     */
    override fun pause() {
        getRemoteMediaClient()?.pause()
    }

    /**
     * Seek to given position on remote client
     * @param position position at which player must seek to
     */
    override fun seekTo(position: Long) {
        val mediaSeekOptions = MediaSeekOptions.Builder().setPosition(position).build()
        getRemoteMediaClient()?.seek(mediaSeekOptions)
    }

    /**
     * Fast forward by given amount
     * @param amount
     */
    override fun fastForward(amount: Long) {
        getRemoteMediaClient()?.let {
            val newPosition = it.approximateStreamPosition + amount
            val mediaSeekOptions =
                MediaSeekOptions.Builder().setPosition(newPosition).build()
            it.seek(mediaSeekOptions)
        }
    }

    /**
     * Rewind by given amount
     * @param amount
     */
    override fun rewind(amount: Long) {
        getRemoteMediaClient()?.let {
            val newPosition = kotlin.math.max(it.approximateStreamPosition + amount, 0L)
            val mediaSeekOptions =
                MediaSeekOptions.Builder().setPosition(newPosition).build()
            it.seek(mediaSeekOptions)
        }
    }

    /**
     * Return current position, if remote media client is setup
     * @return current position
     */
    override fun currentPosition(): Long? {
        return getRemoteMediaClient()?.approximateStreamPosition
    }

    /**
     * Internal use: return remote media client of current cast session
     */
    private fun getRemoteMediaClient(): RemoteMediaClient? {
        return casterSession.castSession?.remoteMediaClient
    }

    /**
     * Add cast session listener in onResume event of host component
     * Must be called in onResume of host Activity/Fragment
     */
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

    /**
     * Remove cast session listener in onPause event of host component
     * Must be called in onPause of host Activity/Fragment
     */
    override fun onPause() {
        if (this::sessionManagerListener.isInitialized.not()) {
            return
        }
        castContext.sessionManager.removeSessionManagerListener(
            sessionManagerListener, CastSession::class.java
        )
    }

    override fun release() {
        Log.d("Cast", "release: ")
        castContext.sessionManager.endCurrentSession(true)
    }
}