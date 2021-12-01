package tv.mycujoo.mcls.tv.internal.transport

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.leanback.media.PlaybackGlueHost
import androidx.leanback.media.PlayerAdapter
import androidx.leanback.widget.*
import tv.mycujoo.mcls.api.MLSTVConfiguration
import tv.mycujoo.mcls.tv.internal.controller.ControllerAgent
import tv.mycujoo.mcls.tv.internal.presenter.MLSPlaybackTransportRowPresenter
import tv.mycujoo.mcls.tv.widgets.MLSFastForwardAction
import tv.mycujoo.mcls.tv.widgets.MLSPlayPauseAction
import tv.mycujoo.mcls.tv.widgets.MLSRewindAction
import java.lang.ref.WeakReference

class MLSPlaybackTransportControlGlueImplKt<AdapterT : PlayerAdapter> constructor(
    context: Context,
    impl: AdapterT,
    config: MLSTVConfiguration,
    controllerAgent: ControllerAgent
) : MLSPlaybackBaseControlGlueKt<AdapterT>(context, impl, config, controllerAgent) {

    companion object {
        private const val TAG = "MLSPlaybackTransportCon"
        private const val DEBUG = false

        internal const val MSG_UPDATE_PLAYBACK_STATE = 100
        internal const val UPDATE_PLAYBACK_STATE_DELAY_MS = 2000
    }

    @Suppress("UNCHECKED_CAST")
    inner class UpdatePlaybackStateHandler<AdapterT : PlayerAdapter> constructor(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_UPDATE_PLAYBACK_STATE) {
                val glue = (msg.obj as WeakReference<MLSPlaybackTransportControlGlueImplKt<AdapterT>>).get()
                glue?.onUpdatePlaybackState()
            }
        }
    }

    private val sHandler = UpdatePlaybackStateHandler<AdapterT>(Looper.myLooper()!!)
    var mSeekProvider: PlaybackSeekDataProvider? = null
    var mSeekEnabled = true

    private val mGlueWeakReference: WeakReference<MLSPlaybackTransportControlGlueImplKt<AdapterT>> =
        WeakReference(this)

    private val mPlaybackSeekUiClient = SeekUiClient()

    override fun setControlsRow(controlsRow: PlaybackControlsRow) {
        super.setControlsRow(controlsRow)
        sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference)
        onUpdatePlaybackState()
    }

    override fun onCreatePrimaryActions(primaryActionsAdapter: ArrayObjectAdapter) {
        val videoPlayerConfig = config.videoPlayerConfig
        if (videoPlayerConfig.showBackForwardsButtons) {
            mRewindAction = MLSRewindAction(context, 1, videoPlayerConfig.primaryColor)
            primaryActionsAdapter.add(mRewindAction)
        }

        mPlayPauseAction = MLSPlayPauseAction(
            context,
            videoPlayerConfig.primaryColor
        )
        primaryActionsAdapter.add(mPlayPauseAction)

        if (videoPlayerConfig.showBackForwardsButtons) {
            mFastForwardAction = MLSFastForwardAction(context, 1, videoPlayerConfig.primaryColor)
            primaryActionsAdapter.add(mFastForwardAction)
        }
    }

    override fun onCreateRowPresenter(liveToggleL: ControllerAgent): PlaybackRowPresenter {
        val detailsPresenter = object : AbstractDetailsDescriptionPresenter() {
            override fun onBindDescription(vh: ViewHolder?, item: Any?) {
                val glue = item as MLSPlaybackBaseControlGlueKt<*>
                vh?.title?.text = glue.getTitle()
                vh?.subtitle?.text = glue.getSubtitle()
            }
        }

        Log.d(TAG, "onCreateRowPresenter: ")

        val rowPresenter = object : MLSPlaybackTransportRowPresenter(controllerAgent, config) {
            override fun onBindRowViewHolder(holder: RowPresenter.ViewHolder?, item: Any?) {
                super.onBindRowViewHolder(holder, item)
                holder?.onKeyListener = this@MLSPlaybackTransportControlGlueImplKt
            }

            override fun onUnbindRowViewHolder(holder: RowPresenter.ViewHolder?) {
                super.onUnbindRowViewHolder(holder)
                holder?.onKeyListener = null
            }
        }

        rowPresenter.setDescriptionPresenter(detailsPresenter)
        return rowPresenter
    }

    override fun onAttachedToHost(host: PlaybackGlueHost?) {
        super.onAttachedToHost(host)
        if (host is PlaybackSeekUi) {
            host.setPlaybackSeekUiClient(mPlaybackSeekUiClient)
        }
    }

    override fun onDetachedFromHost() {
        super.onDetachedFromHost()
        val currentHost = host
        if (currentHost is PlaybackSeekUi) {
            currentHost.setPlaybackSeekUiClient(null)
        }
    }

    override fun onUpdateProgress() {
        if (!mPlaybackSeekUiClient.mIsSeek) {
            super.onUpdateProgress()
        }
    }


    override fun onActionClicked(action: Action) {
        dispatchAction(action, null)
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_ESCAPE -> return false
        }

        val primaryActionsAdapter = getControlsRow()?.primaryActionsAdapter
        var action = getControlsRow()?.getActionForKeyCode(primaryActionsAdapter, keyCode)
        if (action == null) {
            action = getControlsRow()?.getActionForKeyCode(
                getControlsRow()?.secondaryActionsAdapter,
                keyCode
            )
        }

        if (action != null) {
            if (event!!.action == KeyEvent.ACTION_DOWN) {
                dispatchAction(action, event)
            }
            return true
        }
        return false
    }


    private fun onUpdatePlaybackStatusAfterUserAction() {
        updatePlaybackState(isPlaying)

        sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference)
        sHandler.sendMessageDelayed(
            sHandler.obtainMessage(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference),
            UPDATE_PLAYBACK_STATE_DELAY_MS.toLong()
        )
    }

    private fun dispatchAction(action: Action, keyEvent: KeyEvent?): Boolean {
        //            PLAY_PAUSE    PLAY      PAUSE
        // playing    paused                  paused
        // paused     playing       playing
        // ff/rw      playing       playing   paused
        var handled = false
        when (action) {
            is MLSPlayPauseAction -> {
                val canPlay =
                    keyEvent == null || keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY
                val canPause =
                    keyEvent == null || keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyEvent.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
                if (canPause && mIsPlaying) {
                    mIsPlaying = false
                    pause()
                } else if (canPlay && !mIsPlaying) {
                    mIsPlaying = true
                    play()
                }
                onUpdatePlaybackStatusAfterUserAction()
                handled = true
            }
            is PlaybackControlsRow.SkipNextAction -> {
                next()
                handled = true
            }
            is PlaybackControlsRow.SkipPreviousAction -> {
                previous()
                handled = true
            }
            is MLSRewindAction -> {
                rewind()
                handled = true
            }
            is MLSFastForwardAction -> {
                fastForward()
                handled = true
            }
        }

        return handled
    }

    override fun onPlayStateChanged() {
        if (DEBUG) Log.v(TAG, "onPlayStateChanged: ")

        val hasMessage = sHandler.hasMessages(
            MSG_UPDATE_PLAYBACK_STATE,
            mGlueWeakReference
        )

        if (hasMessage) {
            sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference)

            if (mPlayerAdapter.isPlaying != mIsPlaying) {
                if (DEBUG) {
                    Log.v(
                        TAG,
                        "onPlayStateChanged: Status expectation mismatch, delaying update"
                    )
                }
                sHandler.sendMessageDelayed(
                    sHandler.obtainMessage(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference),
                    UPDATE_PLAYBACK_STATE_DELAY_MS.toLong()
                )
            } else {
                if (DEBUG) Log.v(TAG, "onPlayStateChanged: Update state matches expectation")
                onUpdatePlaybackState()
            }
        } else {
            onUpdatePlaybackState()
        }

        super.onPlayStateChanged()
    }

    fun onUpdatePlaybackState() {
        mIsPlaying = mPlayerAdapter.isPlaying
        updatePlaybackState(mIsPlaying)
    }

    private fun updatePlaybackState(isPlaying: Boolean) {
        if (mControlsRow == null) {
            return
        }

        if (!isPlaying) {
            onUpdateProgress()
            mPlayerAdapter.setProgressUpdatingEnabled(mPlaybackSeekUiClient.mIsSeek)
        } else {
            mPlayerAdapter.setProgressUpdatingEnabled(true)
        }

        if (mFadeWhenPlaying && host != null) {
            host.isControlsOverlayAutoHideEnabled = isPlaying
        }

        val index =
            if (!isPlaying) PlaybackControlsRow.PlayPauseAction.INDEX_PLAY else PlaybackControlsRow.PlayPauseAction.INDEX_PAUSE
        if (mPlayPauseAction.index != index) {
            mPlayPauseAction.index = index
            notifyItemChanged(
                getControlsRow()?.primaryActionsAdapter as ArrayObjectAdapter,
                mPlayPauseAction
            )
        }
    }


    /**
     * Set seek data provider used during user seeking.
     *
     * @param seekProvider Seek data provider used during user seeking.
     */
    fun setSeekProvider(seekProvider: PlaybackSeekDataProvider) {
        mSeekProvider = seekProvider
    }

    /**
     * Get seek data provider used during user seeking.
     *
     * @return Seek data provider used during user seeking.
     */
    fun getSeekProvider(): PlaybackSeekDataProvider? {
        return mSeekProvider
    }

    /**
     * Enable or disable seek when [.getSeekProvider] is null. When true,
     * [PlayerAdapter.seekTo] will be called during user seeking.
     *
     * @param seekEnabled True to enable seek, false otherwise
     */
    fun setSeekEnabled(seekEnabled: Boolean) {
        mSeekEnabled = seekEnabled
    }

    /**
     * @return True if seek is enabled without [PlaybackSeekDataProvider], false otherwise.
     */
    fun isSeekEnabled(): Boolean {
        return mSeekEnabled
    }

    inner class SeekUiClient : PlaybackSeekUi.Client() {
        private var mPausedBeforeSeek = false
        private var mPositionBeforeSeek: Long = 0
        private var mLastUserPosition: Long = 0
        var mIsSeek = false

        override fun getPlaybackSeekDataProvider(): PlaybackSeekDataProvider? {
            return mSeekProvider
        }

        override fun isSeekEnabled(): Boolean = mSeekProvider != null && mSeekEnabled

        override fun onSeekStarted() {
            mIsSeek = true
            mPausedBeforeSeek = !isPlaying
            mPlayerAdapter.setProgressUpdatingEnabled(true)
            mPositionBeforeSeek = if (mSeekProvider == null) {
                mPlayerAdapter.currentPosition
            } else {
                -1
            }

            mLastUserPosition = -1
            pause()
        }

        override fun onSeekPositionChanged(pos: Long) {
            if (mSeekProvider == null) {
                mPlayerAdapter.seekTo(pos)
            } else {
                mLastUserPosition = pos
            }

            mControlsRow?.currentPosition = pos
        }

        override fun onSeekFinished(cancelled: Boolean) {
            if (!cancelled) {
                if (mLastUserPosition >= 0) {
                    seekTo(mLastUserPosition)
                }
            } else {
                if (mPositionBeforeSeek >= 0) {
                    seekTo(mPositionBeforeSeek)
                }
            }
            mIsSeek = false
            if (!mPausedBeforeSeek) {
                play()
            } else {
                mPlayerAdapter.setProgressUpdatingEnabled(false)
                // we need update UI since PlaybackControlRow still saves previous position.
                onUpdateProgress()
            }
        }
    }
}