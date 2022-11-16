package tv.mycujoo.mcls.tv.internal.transport

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.annotation.CallSuper
import androidx.leanback.media.PlaybackGlue
import androidx.leanback.media.PlaybackGlueHost
import androidx.leanback.media.PlayerAdapter
import androidx.leanback.widget.*
import timber.log.Timber
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.tv.internal.controller.ControllerAgent
import tv.mycujoo.mcls.tv.widgets.MLSFastForwardAction
import tv.mycujoo.mcls.tv.widgets.MLSPlayPauseAction
import tv.mycujoo.mcls.tv.widgets.MLSRewindAction

abstract class MLSPlaybackBaseControlGlueKt<T : PlayerAdapter> constructor(
    context: Context,
    val mPlayerAdapter: T,
    val config: MLSTVConfiguration,
    val controllerAgent: ControllerAgent
) : PlaybackGlue(context),
    OnActionClickedListener,
    View.OnKeyListener {

    companion object {
        /**
         * The adapter key for the first custom control on the left side
         * of the predefined primary controls.
         */
        const val ACTION_CUSTOM_LEFT_FIRST = 0x1

        /**
         * The adapter key for the skip to previous control.
         */
        const val ACTION_SKIP_TO_PREVIOUS = 0x10

        /**
         * The adapter key for the rewind control.
         */
        const val ACTION_REWIND = 0x20

        /**
         * The adapter key for the play/pause control.
         */
        const val ACTION_PLAY_PAUSE = 0x40

        /**
         * The adapter key for the fast forward control.
         */
        const val ACTION_FAST_FORWARD = 0x80

        /**
         * The adapter key for the skip to next control.
         */
        const val ACTION_SKIP_TO_NEXT = 0x100

        /**
         * The adapter key for the repeat control.
         */
        const val ACTION_REPEAT = 0x200

        /**
         * The adapter key for the shuffle control.
         */
        const val ACTION_SHUFFLE = 0x400

        /**
         * The adapter key for the first custom control on the right side
         * of the predefined primary controls.
         */
        const val ACTION_CUSTOM_RIGHT_FIRST = 0x1000

        private const val TAG = "MLSPlaybackBaseControlG"

        const val DEBUG = false

        fun notifyItemChanged(adapter: ArrayObjectAdapter, obj: Any?) {
            if (obj == null) {
                return
            }

            val index = adapter.indexOf(obj)
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1)
            }
        }
    }

    var mControlsRow: PlaybackControlsRow? = null
    private var mControlsRowPresenter: PlaybackRowPresenter? = null

    lateinit var mPlayPauseAction: MLSPlayPauseAction
    lateinit var mRewindAction: MLSRewindAction
    lateinit var mFastForwardAction: MLSFastForwardAction

    var mIsPlaying = false
    var mFadeWhenPlaying = true

    private var mSubtitle: String? = null
    private var mTitle: String? = null
    private var mCover: Drawable? = null

    private var mPlayerCallback: PlaybackGlueHost.PlayerCallback? = null
    private var mBuffering = false
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mErrorSet = false
    private var mErrorCode: Int? = null
    private var mErrorMessage: String? = null

    private val mAdapterCallback = object : PlayerAdapter.Callback() {
        override fun onPlayStateChanged(adapter: PlayerAdapter) {
            if (DEBUG) Timber.tag(TAG).v("onPlayStateChanged")
            onPlayStateChanged()
        }

        override fun onCurrentPositionChanged(adapter: PlayerAdapter) {
            if (DEBUG) Timber.tag(TAG).v("onPlayStateChanged")
            onUpdateProgress()
        }

        override fun onBufferedPositionChanged(adapter: PlayerAdapter) {
            if (DEBUG) Timber.tag(TAG).v("onBufferedPositionChanged: ")
            onUpdateBufferedProgress()
        }

        override fun onDurationChanged(adapter: PlayerAdapter) {
            if (DEBUG) Timber.tag(TAG).v("onDurationChanged: ")
            onUpdateDuration()
        }

        override fun onPlayCompleted(adapter: PlayerAdapter) {
            if (DEBUG) Timber.tag(TAG).v("onPlayCompleted: ")
            onPlayCompleted()
        }

        override fun onPreparedStateChanged(adapter: PlayerAdapter) {
            if (DEBUG) Timber.tag(TAG).i("onPreparedStateChanged: ")
            onPreparedStateChanged()
        }

        override fun onVideoSizeChanged(adapter: PlayerAdapter, width: Int, height: Int) {
            mVideoWidth = width
            mVideoHeight = height
            mPlayerCallback?.onVideoSizeChanged(width, height)
        }

        override fun onError(adapter: PlayerAdapter, errorCode: Int, errorMessage: String?) {
            mErrorSet = true
            mErrorCode = errorCode
            mErrorMessage = errorMessage
            mPlayerCallback?.onError(errorCode, errorMessage)
        }

        override fun onBufferingStateChanged(adapter: PlayerAdapter, start: Boolean) {
            mBuffering = start
            controllerAgent.onBufferingStateChanged(start)
        }

        override fun onMetadataChanged(adapter: PlayerAdapter) {
            onMetadataChanged()
        }
    }

    init {
        mPlayerAdapter.callback = mAdapterCallback
    }


    fun getPlayerAdapter(): T {
        return mPlayerAdapter
    }

    override fun onAttachedToHost(host: PlaybackGlueHost) {
        super.onAttachedToHost(host)
        host.setOnKeyInterceptListener(this)
        host.setOnActionClickedListener(this)
        onCreateDefaultControlsRow()
        onCreateDefaultRowPresenter()
        host.setPlaybackRowPresenter(getPlaybackRowPresenter())
        host.setPlaybackRow(getControlsRow())

        mPlayerCallback = host.playerCallback
        onAttachHostCallback()
        mPlayerAdapter.onAttachedToHost(host)
    }

    private fun onAttachHostCallback() {
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            mPlayerCallback?.onVideoSizeChanged(mVideoWidth, mVideoHeight)
        }

        if (mErrorSet) {
            mErrorCode?.let { errorCode ->
                mPlayerCallback?.onError(errorCode, mErrorMessage)
            }
        }

        // mPlayerCallback removed and replaced
        controllerAgent.onBufferingStateChanged(mBuffering)
    }

    private fun onDetachHostCallback() {
        mErrorSet = false
        mErrorCode = 0
        mErrorMessage = null

        // mPlayerCallback removed and replaced
        controllerAgent.onBufferingStateChanged(false)
    }

    override fun onHostStart() {
        mPlayerAdapter.setProgressUpdatingEnabled(true)
    }

    override fun onHostStop() {
        mPlayerAdapter.setProgressUpdatingEnabled(false)
    }

    override fun onDetachedFromHost() {
        onDetachHostCallback()
        mPlayerCallback = null
        mPlayerAdapter.onDetachedFromHost()
        mPlayerAdapter.setProgressUpdatingEnabled(false)
        super.onDetachedFromHost()
    }

    private fun onCreateDefaultControlsRow() {
        if (mControlsRow == null) {
            val controlsRow = PlaybackControlsRow(this)
            setControlsRow(controlsRow)
        }

    }

    private fun onCreateDefaultRowPresenter() {
        if (mControlsRowPresenter == null) {
            setPlaybackRowPresenter(onCreateRowPresenter(controllerAgent))
        }
    }

    protected abstract fun onCreateRowPresenter(liveToggleL: ControllerAgent): PlaybackRowPresenter

    /**
     * Sets the controls to auto hide after a timeout when media is playing.
     *
     * @param enable True to enable auto hide after a timeout when media is playing.
     * @see PlaybackGlueHost#setControlsOverlayAutoHideEnabled(boolean)
     */
    fun setControlsOverlayAutoHideEnabled(enable: Boolean) {
        mFadeWhenPlaying = enable
        if (mFadeWhenPlaying.not() && host != null) {
            host?.isControlsOverlayAutoHideEnabled = false
        }
    }

    /**
     * Returns true if the controls auto hides after a timeout when media is playing.
     *
     * @see PlaybackGlueHost#isControlsOverlayAutoHideEnabled()
     */
    fun isControlsOverlayAutoHideEnabled(): Boolean = mFadeWhenPlaying

    /**
     * Sets the controls row to be managed by the glue layer. If
     * {@link PlaybackControlsRow#getPrimaryActionsAdapter()} is not provided, a default
     * {@link ArrayObjectAdapter} will be created and initialized in
     * {@link #onCreatePrimaryActions(ArrayObjectAdapter)}. If
     * {@link PlaybackControlsRow#getSecondaryActionsAdapter()} is not provided, a default
     * {@link ArrayObjectAdapter} will be created and initialized in
     * {@link #onCreateSecondaryActions(ArrayObjectAdapter)}.
     * The primary actions and playback state related aspects of the row
     * are updated by the glue.
     */
    open fun setControlsRow(controlsRow: PlaybackControlsRow) {
        Log.d(TAG, "setControlsRow: ")

        mControlsRow = controlsRow
        mControlsRow?.currentPosition = -1
        mControlsRow?.duration = -1
        mControlsRow?.bufferedPosition = -1

        if (mControlsRow?.primaryActionsAdapter == null) {
            val adapter = ArrayObjectAdapter(ControlButtonPresenterSelector())
            onCreatePrimaryActions(adapter)
            mControlsRow?.primaryActionsAdapter = adapter
        }

        // Add secondary actions
        if (mControlsRow?.secondaryActionsAdapter == null) {
            val secondaryActions = ArrayObjectAdapter(ControlButtonPresenterSelector())
            onCreateSecondaryActions(secondaryActions)
            controlsRow.secondaryActionsAdapter = secondaryActions
        }
        updateControlsRow()
    }

    /**
     * Sets the controls row Presenter to be managed by the glue layer.
     */
    private fun setPlaybackRowPresenter(presenter: PlaybackRowPresenter) {
        mControlsRowPresenter = presenter
    }

    /**
     * Returns the playback controls row managed by the glue layer.
     */
    fun getControlsRow() = mControlsRow

    /**
     * Returns the playback controls row Presenter managed by the glue layer.
     */
    private fun getPlaybackRowPresenter() = mControlsRowPresenter

    abstract override fun onActionClicked(action: Action)

    /**
     * Handles key events and returns true if handled.  A subclass may override this to provide
     * additional support.
     */
    abstract override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean

    private fun updateControlsRow() = onMetadataChanged()

    override fun isPlaying(): Boolean = mPlayerAdapter.isPlaying

    override fun play() = mPlayerAdapter.play()

    override fun pause() = mPlayerAdapter.pause()

    override fun next() = mPlayerAdapter.next()

    override fun previous() = mPlayerAdapter.previous()

    fun rewind() {
        val currentPosition: Long = getCurrentPosition()

        if (currentPosition < 0) return

        if (currentPosition > 10000L) {
            seekTo(currentPosition - 10000L)
        } else {
            seekTo(0)
        }
    }

    fun fastForward() {
        val currentPosition: Long = getCurrentPosition()
        val duration: Long = getDuration()
        if (currentPosition < 0 || duration < 0) {
            return
        }

        if (currentPosition + 1000L < duration) {
            seekTo(currentPosition + 10000L)
        } else {
            seekTo(duration)
        }
    }

    /**
     * May be overridden to add primary actions to the adapter. Default implementation add
     * {@link PlaybackControlsRow.PlayPauseAction}.
     *
     * @param primaryActionsAdapter The adapter to add primary {@link Action}s.
     */
    protected abstract fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter)

    /**
     * May be overridden to add secondary actions to the adapter.
     *
     * @param secondaryActionsAdapter The adapter you need to add the {@link Action}s to.
     */
    protected open fun onCreateSecondaryActions(secondaryActionsAdapter: ArrayObjectAdapter) {
    }

    @CallSuper
    protected open fun onUpdateProgress() {
        mControlsRow?.currentPosition = if (mPlayerAdapter.isPrepared) {
            getCurrentPosition()
        } else {
            -1
        }
    }

    @CallSuper
    protected fun onUpdateBufferedProgress() {
        mControlsRow?.bufferedPosition = mPlayerAdapter.bufferedPosition
    }

    @CallSuper
    protected fun onUpdateDuration() {
        mControlsRow?.duration = if (mPlayerAdapter.isPrepared) {
            mPlayerAdapter.duration
        } else {
            -1
        }
    }

    /**
     * @return The duration of the media item in milliseconds.
     */
    private fun getDuration() = mPlayerAdapter.duration

    /**
     * @return The current position of the media item in milliseconds.
     */
    private fun getCurrentPosition() = mPlayerAdapter.currentPosition

    /**
     * @return The current buffered position of the media item in milliseconds.
     */
    fun getBufferedPosition() = mPlayerAdapter.bufferedPosition

    override fun isPrepared() = mPlayerAdapter.isPrepared

    /**
     * Event when ready state for play changes.
     */
    @CallSuper
    fun onPreparedStateChanged() {
        onUpdateDuration()
        playerCallbacks?.forEach {
            it.onPreparedStateChanged(this)
        }
    }

    /**
     * Sets the drawable representing cover image. The drawable will be rendered by default
     * description presenter in
     * {@link androidx.leanback.widget.PlaybackTransportRowPresenter#setDescriptionPresenter(Presenter)}.
     *
     * @param cover The drawable representing cover image.
     */
    fun setArt(cover: Drawable) {
        if (mCover == cover) return

        mCover = cover
        mControlsRow?.imageDrawable = mCover
        host?.notifyPlaybackRowChanged()
    }

    /**
     * @return The drawable representing cover image.
     */
    private fun getArt() = mCover

    /**
     * Sets the media subtitle. The subtitle will be rendered by default description presenter
     * {@link androidx.leanback.widget.PlaybackTransportRowPresenter#setDescriptionPresenter(Presenter)}.
     *
     * @param subtitle Subtitle to set.
     */
    fun setSubtitle(subtitle: String) {
        if (mSubtitle == subtitle) return

        mSubtitle = subtitle
        host?.notifyPlaybackRowChanged()
    }

    /**
     * Return The media subtitle.
     */
    fun getSubtitle() = mSubtitle

    /**
     * Sets the media title. The title will be rendered by default description presenter
     * {@link androidx.leanback.widget.PlaybackTransportRowPresenter#setDescriptionPresenter(Presenter)}}.
     */
    fun setTitle(title: String) {
        if (mTitle == title) return

        mTitle = title
        host?.notifyPlaybackRowChanged()
    }

    /**
     * Returns the title of the media item.
     */
    fun getTitle() = mTitle

    /**
     * Event when metadata changed
     */
    protected fun onMetadataChanged() {
        if (mControlsRow == null) return

        if (DEBUG) Log.v(TAG, "updateRowMetadata")

        mControlsRow?.imageDrawable = getArt()
        mControlsRow?.duration = getDuration()
        mControlsRow?.currentPosition = getCurrentPosition()
        host?.notifyPlaybackRowChanged()
    }

    /**
     * Event when play state changed.
     */
    @CallSuper
    protected open fun onPlayStateChanged() {
        playerCallbacks?.forEach { it.onPlayStateChanged(this) }
    }

    /**
     * Event when play finishes, subclass may handling repeat mode here.
     */
    @CallSuper
    protected fun onPlayCompleted() {
        playerCallbacks?.forEach { it.onPlayCompleted(this) }
    }

    /**
     * Seek media to a new position.
     *
     * @param position New position.
     */
    fun seekTo(position: Long) {
        mPlayerAdapter.seekTo(position)
    }

    /**
     * Returns a bitmask of actions supported by the media player.
     */
    fun getSupportedActions() = mPlayerAdapter.supportedActions
}