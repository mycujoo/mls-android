package tv.mycujoo.mls.tv.internal.transport;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.leanback.media.PlaybackGlueHost;
import androidx.leanback.media.PlayerAdapter;
import androidx.leanback.widget.AbstractDetailsDescriptionPresenter;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackRowPresenter;
import androidx.leanback.widget.PlaybackSeekDataProvider;
import androidx.leanback.widget.PlaybackSeekUi;
import androidx.leanback.widget.RowPresenter;

import java.lang.ref.WeakReference;

import tv.mycujoo.mls.tv.internal.controller.LiveBadgeStateHandler;
import tv.mycujoo.mls.tv.internal.presenter.MLSPlaybackTransportRowPresenter;
import tv.mycujoo.mls.tv.widgets.MLSFastForwardAction;
import tv.mycujoo.mls.tv.widgets.MLSPlayPauseAction;
import tv.mycujoo.mls.tv.widgets.MLSRewindAction;

public class MLSPlaybackTransportControlGlueImpl<T extends PlayerAdapter> extends MLSPlaybackBaseControlGlue<T> {
    static final String TAG = "PlaybackTransportGlue";
    static final boolean DEBUG = false;

    static final int MSG_UPDATE_PLAYBACK_STATE = 100;
    static final int UPDATE_PLAYBACK_STATE_DELAY_MS = 2000;

    PlaybackSeekDataProvider mSeekProvider;
    boolean mSeekEnabled;

    static class UpdatePlaybackStateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_PLAYBACK_STATE) {
                MLSPlaybackTransportControlGlueImpl glue =
                        ((WeakReference<MLSPlaybackTransportControlGlueImpl>) msg.obj).get();
                if (glue != null) {
                    glue.onUpdatePlaybackState();
                }
            }
        }
    }

    static final Handler sHandler = new UpdatePlaybackStateHandler();

    final WeakReference<MLSPlaybackTransportControlGlueImpl> mGlueWeakReference = new WeakReference(this);

    /**
     * Constructor for the glue.
     *
     * @param context
     * @param impl    Implementation to underlying media player.
     */
    public MLSPlaybackTransportControlGlueImpl(Context context, T impl, LiveBadgeStateHandler liveBadgeStateHandler) {
        super(context, impl, liveBadgeStateHandler);
    }

    @Override
    public void setControlsRow(PlaybackControlsRow controlsRow) {
        super.setControlsRow(controlsRow);
        sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference);
        onUpdatePlaybackState();
    }

    @Override
    protected void onCreatePrimaryActions(ArrayObjectAdapter primaryActionsAdapter) {
        primaryActionsAdapter.add(mRewindAction = new MLSRewindAction(getContext(), 1));
        primaryActionsAdapter.add(mPlayPauseAction =
                new MLSPlayPauseAction(getContext()));
        primaryActionsAdapter.add(mFastForwardAction = new MLSFastForwardAction(getContext(), 1));
    }

    @Override
    protected PlaybackRowPresenter onCreateRowPresenter(LiveBadgeStateHandler liveBadgeStateHandler) {
        final AbstractDetailsDescriptionPresenter detailsPresenter =
                new AbstractDetailsDescriptionPresenter() {
                    @Override
                    protected void onBindDescription(ViewHolder
                                                             viewHolder, Object obj) {
                        MLSPlaybackBaseControlGlue glue = (MLSPlaybackBaseControlGlue) obj;
                        viewHolder.getTitle().setText(glue.getTitle());
                        viewHolder.getSubtitle().setText(glue.getSubtitle());
                    }
                };

        MLSPlaybackTransportRowPresenter rowPresenter = new MLSPlaybackTransportRowPresenter(liveBadgeStateHandler) {
            @Override
            protected void onBindRowViewHolder(RowPresenter.ViewHolder vh, Object item) {
                super.onBindRowViewHolder(vh, item);
                vh.setOnKeyListener(MLSPlaybackTransportControlGlueImpl.this);
            }

            @Override
            protected void onUnbindRowViewHolder(RowPresenter.ViewHolder vh) {
                super.onUnbindRowViewHolder(vh);
                vh.setOnKeyListener(null);
            }
        };
        rowPresenter.setDescriptionPresenter(detailsPresenter);
        return rowPresenter;
    }

    @Override
    protected void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);

        if (host instanceof PlaybackSeekUi) {
            ((PlaybackSeekUi) host).setPlaybackSeekUiClient(mPlaybackSeekUiClient);
        }
    }

    @Override
    protected void onDetachedFromHost() {
        super.onDetachedFromHost();

        if (getHost() instanceof PlaybackSeekUi) {
            ((PlaybackSeekUi) getHost()).setPlaybackSeekUiClient(null);
        }
    }

    @Override
    protected void onUpdateProgress() {
        if (!mPlaybackSeekUiClient.mIsSeek) {
            super.onUpdateProgress();
        }
    }

    @Override
    public void onActionClicked(Action action) {
        dispatchAction(action, null);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                return false;
        }


        final ObjectAdapter primaryActionsAdapter = getControlsRow().getPrimaryActionsAdapter();
        Action action = getControlsRow().getActionForKeyCode(primaryActionsAdapter, keyCode);
        if (action == null) {
            action = getControlsRow().getActionForKeyCode(getControlsRow().getSecondaryActionsAdapter(),
                    keyCode);
        }

        if (action != null) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                dispatchAction(action, event);
            }
            return true;
        }
        return false;
    }

    void onUpdatePlaybackStatusAfterUserAction() {
        updatePlaybackState(isPlaying());

        // Sync playback state after a delay
        sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference);
        sHandler.sendMessageDelayed(sHandler.obtainMessage(MSG_UPDATE_PLAYBACK_STATE,
                mGlueWeakReference), UPDATE_PLAYBACK_STATE_DELAY_MS);
    }

    /**
     * Called when the given action is invoked, either by click or keyevent.
     */
    boolean dispatchAction(Action action, KeyEvent keyEvent) {
        boolean handled = false;
        if (action instanceof MLSPlayPauseAction) {
            boolean canPlay = keyEvent == null
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY;
            boolean canPause = keyEvent == null
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    || keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE;
            //            PLAY_PAUSE    PLAY      PAUSE
            // playing    paused                  paused
            // paused     playing       playing
            // ff/rw      playing       playing   paused
            if (canPause && mIsPlaying) {
                mIsPlaying = false;
                pause();
            } else if (canPlay && !mIsPlaying) {
                mIsPlaying = true;
                play();
            }
            onUpdatePlaybackStatusAfterUserAction();
            handled = true;
        } else if (action instanceof PlaybackControlsRow.SkipNextAction) {
            next();
            handled = true;
        } else if (action instanceof PlaybackControlsRow.SkipPreviousAction) {
            previous();
            handled = true;
        } else if (action instanceof MLSRewindAction) {
            rewind();
            handled = true;
        } else if (action instanceof MLSFastForwardAction) {
            fastForward();
            handled = true;
        }
        return handled;
    }

    @Override
    protected void onPlayStateChanged() {
        if (DEBUG) Log.v(TAG, "onStateChanged");

        if (sHandler.hasMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference)) {
            sHandler.removeMessages(MSG_UPDATE_PLAYBACK_STATE, mGlueWeakReference);
            if (mPlayerAdapter.isPlaying() != mIsPlaying) {
                if (DEBUG) Log.v(TAG, "Status expectation mismatch, delaying update");
                sHandler.sendMessageDelayed(sHandler.obtainMessage(MSG_UPDATE_PLAYBACK_STATE,
                        mGlueWeakReference), UPDATE_PLAYBACK_STATE_DELAY_MS);
            } else {
                if (DEBUG) Log.v(TAG, "Update state matches expectation");
                onUpdatePlaybackState();
            }
        } else {
            onUpdatePlaybackState();
        }

        super.onPlayStateChanged();
    }

    void onUpdatePlaybackState() {
        mIsPlaying = mPlayerAdapter.isPlaying();
        updatePlaybackState(mIsPlaying);
    }

    private void updatePlaybackState(boolean isPlaying) {
        if (mControlsRow == null) {
            return;
        }

        if (!isPlaying) {
            onUpdateProgress();
            mPlayerAdapter.setProgressUpdatingEnabled(mPlaybackSeekUiClient.mIsSeek);
        } else {
            mPlayerAdapter.setProgressUpdatingEnabled(true);
        }

        if (mFadeWhenPlaying && getHost() != null) {
            getHost().setControlsOverlayAutoHideEnabled(isPlaying);
        }

        if (mPlayPauseAction != null) {
            int index = !isPlaying
                    ? PlaybackControlsRow.PlayPauseAction.INDEX_PLAY
                    : PlaybackControlsRow.PlayPauseAction.INDEX_PAUSE;
            if (mPlayPauseAction.getIndex() != index) {
                mPlayPauseAction.setIndex(index);
                notifyItemChanged((ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter(),
                        mPlayPauseAction);
            }
        }
    }

    final SeekUiClient mPlaybackSeekUiClient = new SeekUiClient();

    class SeekUiClient extends PlaybackSeekUi.Client {
        boolean mPausedBeforeSeek;
        long mPositionBeforeSeek;
        long mLastUserPosition;
        boolean mIsSeek;

        @Override
        public PlaybackSeekDataProvider getPlaybackSeekDataProvider() {
            return mSeekProvider;
        }

        @Override
        public boolean isSeekEnabled() {
            return mSeekProvider != null || mSeekEnabled;
        }

        @Override
        public void onSeekStarted() {
            mIsSeek = true;
            mPausedBeforeSeek = !isPlaying();
            mPlayerAdapter.setProgressUpdatingEnabled(true);
            // if we seek thumbnails, we don't need save original position because current
            // position is not changed during seeking.
            // otherwise we will call seekTo() and may need to restore the original position.
            mPositionBeforeSeek = mSeekProvider == null ? mPlayerAdapter.getCurrentPosition() : -1;
            mLastUserPosition = -1;
            pause();
        }

        @Override
        public void onSeekPositionChanged(long pos) {
            if (mSeekProvider == null) {
                mPlayerAdapter.seekTo(pos);
            } else {
                mLastUserPosition = pos;
            }
            if (mControlsRow != null) {
                mControlsRow.setCurrentPosition(pos);
            }
        }

        @Override
        public void onSeekFinished(boolean cancelled) {
            if (!cancelled) {
                if (mLastUserPosition >= 0) {
                    seekTo(mLastUserPosition);
                }
            } else {
                if (mPositionBeforeSeek >= 0) {
                    seekTo(mPositionBeforeSeek);
                }
            }
            mIsSeek = false;
            if (!mPausedBeforeSeek) {
                play();
            } else {
                mPlayerAdapter.setProgressUpdatingEnabled(false);
                // we neeed update UI since PlaybackControlRow still saves previous position.
                onUpdateProgress();
            }
        }
    }

    ;

    /**
     * Set seek data provider used during user seeking.
     *
     * @param seekProvider Seek data provider used during user seeking.
     */
    public final void setSeekProvider(PlaybackSeekDataProvider seekProvider) {
        mSeekProvider = seekProvider;
    }

    /**
     * Get seek data provider used during user seeking.
     *
     * @return Seek data provider used during user seeking.
     */
    public final PlaybackSeekDataProvider getSeekProvider() {
        return mSeekProvider;
    }

    /**
     * Enable or disable seek when {@link #getSeekProvider()} is null. When true,
     * {@link PlayerAdapter#seekTo(long)} will be called during user seeking.
     *
     * @param seekEnabled True to enable seek, false otherwise
     */
    public final void setSeekEnabled(boolean seekEnabled) {
        mSeekEnabled = seekEnabled;
    }

    /**
     * @return True if seek is enabled without {@link PlaybackSeekDataProvider}, false otherwise.
     */
    public final boolean isSeekEnabled() {
        return mSeekEnabled;
    }
}
