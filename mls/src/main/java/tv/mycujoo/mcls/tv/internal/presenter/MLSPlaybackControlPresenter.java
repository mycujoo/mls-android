package tv.mycujoo.mcls.tv.internal.presenter;

import android.content.Context;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.Presenter;

import tv.mycujoo.mcls.R;
import tv.mycujoo.mcls.utils.MathUtils;

public class MLSPlaybackControlPresenter extends MLSControlBarPresenter {

    /**
     * The data type expected by this presenter.
     */
    static class BoundData extends MLSControlBarPresenter.BoundData {
        /**
         * The adapter containing secondary actions.
         */
        ObjectAdapter secondaryActionsAdapter;
    }

    class ViewHolder extends MLSControlBarPresenter.ViewHolder {
        ObjectAdapter mMoreActionsAdapter;
        ObjectAdapter.DataObserver mMoreActionsObserver;
        final FrameLayout mMoreActionsDock;
        Presenter.ViewHolder mMoreActionsViewHolder;
        boolean mMoreActionsShowing;
        final TextView mCurrentTime;
        final TextView mTotalTime;
        final ProgressBar mProgressBar;
        long mCurrentTimeInMs = -1;         // Hold current time in milliseconds
        long mTotalTimeInMs = -1;           // Hold total time in milliseconds
        long mSecondaryProgressInMs = -1;   // Hold secondary progress in milliseconds
        StringBuilder mTotalTimeStringBuilder = new StringBuilder();
        StringBuilder mCurrentTimeStringBuilder = new StringBuilder();
        int mCurrentTimeMarginStart;
        int mTotalTimeMarginEnd;

        ViewHolder(View rootView) {
            super(rootView);
            mMoreActionsDock = (FrameLayout) rootView.findViewById(R.id.more_actions_dock);
            mCurrentTime = (TextView) rootView.findViewById(R.id.current_time);
            mTotalTime = (TextView) rootView.findViewById(R.id.total_time);
            mProgressBar = (ProgressBar) rootView.findViewById(R.id.playback_progress);
            mMoreActionsObserver = new ObjectAdapter.DataObserver() {
                @Override
                public void onChanged() {
                    if (mMoreActionsShowing) {
                        showControls(mPresenter);
                    }
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    if (mMoreActionsShowing) {
                        for (int i = 0; i < itemCount; i++) {
                            bindControlToAction(positionStart + i, mPresenter);
                        }
                    }
                }
            };
            mCurrentTimeMarginStart =
                    ((ViewGroup.MarginLayoutParams) mCurrentTime.getLayoutParams()).getMarginStart();
            mTotalTimeMarginEnd =
                    ((ViewGroup.MarginLayoutParams) mTotalTime.getLayoutParams()).getMarginEnd();
        }

        void showMoreActions(boolean show) {
            if (show) {
                if (mMoreActionsViewHolder == null) {
                    Action action = new PlaybackControlsRow.MoreActions(mMoreActionsDock.getContext());
                    mMoreActionsViewHolder = mPresenter.onCreateViewHolder(mMoreActionsDock);
                    mPresenter.onBindViewHolder(mMoreActionsViewHolder, action);
                    mPresenter.setOnClickListener(mMoreActionsViewHolder, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            toggleMoreActions();
                        }
                    });
                }
                if (mMoreActionsViewHolder.view.getParent() == null) {
                    mMoreActionsDock.addView(mMoreActionsViewHolder.view);
                }
            } else if (mMoreActionsViewHolder != null
                    && mMoreActionsViewHolder.view.getParent() != null) {
                mMoreActionsDock.removeView(mMoreActionsViewHolder.view);
            }
        }

        void toggleMoreActions() {
            mMoreActionsShowing = !mMoreActionsShowing;
            showControls(mPresenter);
        }

        @Override
        ObjectAdapter getDisplayedAdapter() {
            return mMoreActionsShowing ? mMoreActionsAdapter : mAdapter;
        }

        @Override
        int getChildMarginFromCenter(Context context, int numControls) {
            int margin = getControlIconWidth(context);
            if (numControls < 4) {
                margin += getChildMarginBiggest(context);
            } else if (numControls < 6) {
                margin += getChildMarginBigger(context);
            } else {
                margin += getChildMarginDefault(context);
            }
            return margin;
        }

        void setTotalTime(long totalTimeMs) {
            if (totalTimeMs <= 0) {
                mTotalTime.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
            } else {
                mTotalTime.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mTotalTimeInMs = totalTimeMs;
                formatTime(totalTimeMs / 1000, mTotalTimeStringBuilder);
                mTotalTime.setText(mTotalTimeStringBuilder.toString());
                mProgressBar.setMax(Integer.MAX_VALUE);//current progress will be a fraction of this
            }
        }

        long getTotalTime() {
            return mTotalTimeInMs;
        }

        void setCurrentTime(long currentTimeMs) {
            long seconds = currentTimeMs / 1000;
            if (currentTimeMs != mCurrentTimeInMs) {
                mCurrentTimeInMs = currentTimeMs;
                formatTime(seconds, mCurrentTimeStringBuilder);
                mCurrentTime.setText(mCurrentTimeStringBuilder.toString());
            }
            // Use ratio to represent current progress
            double ratio = (double) mCurrentTimeInMs / mTotalTimeInMs;     // Range: [0, 1]
            double progressRatio = ratio * Integer.MAX_VALUE;   // Could safely cast to int
            mProgressBar.setProgress((int) progressRatio);
        }

        long getCurrentTime() {
            return mTotalTimeInMs;
        }

        void setSecondaryProgress(long progressMs) {
            mSecondaryProgressInMs = progressMs;
            // Solve the progress bar by using ratio
            double ratio = (double) progressMs / mTotalTimeInMs;           // Range: [0, 1]
            double progressRatio = ratio * Integer.MAX_VALUE;   // Could safely cast to int
            mProgressBar.setSecondaryProgress((int) progressRatio);
        }

        long getSecondaryProgress() {
            return mSecondaryProgressInMs;
        }
    }

    static void formatTime(long seconds, StringBuilder sb) {
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds -= minutes * 60;
        minutes -= hours * 60;

        sb.setLength(0);
        if (hours > 0) {
            sb.append(hours).append(':');
            if (minutes < 10) {
                sb.append('0');
            }
        }
        sb.append(minutes).append(':');
        if (seconds < 10) {
            sb.append('0');
        }
        sb.append(seconds);
    }

    private boolean mMoreActionsEnabled = true;
    private static int sChildMarginBigger;
    private static int sChildMarginBiggest;

    /**
     * Constructor for a PlaybackControlsRowPresenter.
     *
     * @param layoutResourceId The resource id of the layout for this presenter.
     */
    public MLSPlaybackControlPresenter(int layoutResourceId) {
        super(layoutResourceId);
    }

    /**
     * Enables the display of secondary actions.
     * A "more actions" button will be displayed.  When "more actions" is selected,
     * the primary actions are replaced with the secondary actions.
     */
    public void enableSecondaryActions(boolean enable) {
        mMoreActionsEnabled = enable;
    }

    /**
     * Returns true if secondary actions are enabled.
     */
    public boolean areMoreActionsEnabled() {
        return mMoreActionsEnabled;
    }

    public void setProgressColor(ViewHolder vh, @ColorInt int color) {
        Drawable drawable = new ClipDrawable(new ColorDrawable(color),
                Gravity.LEFT, ClipDrawable.HORIZONTAL);
        ((LayerDrawable) vh.mProgressBar.getProgressDrawable())
                .setDrawableByLayerId(android.R.id.progress, drawable);
    }

    public void setTotalTime(MLSPlaybackControlPresenter.ViewHolder vh, int ms) {
        setTotalTimeLong(vh, (long) ms);
    }

    public void setTotalTimeLong(MLSPlaybackControlPresenter.ViewHolder vh, long ms) {
        vh.setTotalTime(ms);
    }

    public int getTotalTime(ViewHolder vh) {
        return MathUtils.Companion.safeLongToInt(getTotalTimeLong(vh));
    }

    public long getTotalTimeLong(ViewHolder vh) {
        return vh.getTotalTime();
    }

    public void setCurrentTime(ViewHolder vh, int ms) {
        setCurrentTimeLong(vh, (long) ms);
    }

    public void setCurrentTimeLong(ViewHolder vh, long ms) {
        vh.setCurrentTime(ms);
    }

    public int getCurrentTime(ViewHolder vh) {
        return MathUtils.Companion.safeLongToInt(getCurrentTimeLong(vh));
    }

    public long getCurrentTimeLong(ViewHolder vh) {
        return vh.getCurrentTime();
    }

    public void setSecondaryProgress(ViewHolder vh, int progressMs) {
        setSecondaryProgressLong(vh, (long) progressMs);
    }

    public void setSecondaryProgressLong(ViewHolder vh, long progressMs) {
        vh.setSecondaryProgress(progressMs);
    }

    public int getSecondaryProgress(ViewHolder vh) {
        return MathUtils.Companion.safeLongToInt(getSecondaryProgressLong(vh));
    }

    public long getSecondaryProgressLong(ViewHolder vh) {
        return vh.getSecondaryProgress();
    }

    public void showPrimaryActions(ViewHolder vh) {
        if (vh.mMoreActionsShowing) {
            vh.toggleMoreActions();
        }
    }

    public void resetFocus(ViewHolder vh) {
        vh.mControlBar.requestFocus();
    }

    public void enableTimeMargins(ViewHolder vh, boolean enable) {
        ViewGroup.MarginLayoutParams lp;
        lp = (ViewGroup.MarginLayoutParams) vh.mCurrentTime.getLayoutParams();
        lp.setMarginStart(enable ? vh.mCurrentTimeMarginStart : 0);
        vh.mCurrentTime.setLayoutParams(lp);

        lp = (ViewGroup.MarginLayoutParams) vh.mTotalTime.getLayoutParams();
        lp.setMarginEnd(enable ? vh.mTotalTimeMarginEnd : 0);
        vh.mTotalTime.setLayoutParams(lp);
    }

    @Override
    public Presenter.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutResourceId(), parent, false);
        return new MLSControlBarPresenter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder holder, Object item) {
        ViewHolder vh = (ViewHolder) holder;
        BoundData data = (BoundData) item;

        // If binding to a new adapter, display primary actions.
        if (vh.mMoreActionsAdapter != data.secondaryActionsAdapter) {
            vh.mMoreActionsAdapter = data.secondaryActionsAdapter;
            vh.mMoreActionsAdapter.registerObserver(vh.mMoreActionsObserver);
            vh.mMoreActionsShowing = false;
        }

        super.onBindViewHolder(holder, item);
        vh.showMoreActions(mMoreActionsEnabled);
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder holder) {
        super.onUnbindViewHolder(holder);
        ViewHolder vh = (ViewHolder) holder;
        if (vh.mMoreActionsAdapter != null) {
            vh.mMoreActionsAdapter.unregisterObserver(vh.mMoreActionsObserver);
            vh.mMoreActionsAdapter = null;
        }
    }

    int getChildMarginBigger(Context context) {
        if (sChildMarginBigger == 0) {
            sChildMarginBigger = context.getResources().getDimensionPixelSize(
                    R.dimen.lb_playback_controls_child_margin_bigger);
        }
        return sChildMarginBigger;
    }

    int getChildMarginBiggest(Context context) {
        if (sChildMarginBiggest == 0) {
            sChildMarginBiggest = context.getResources().getDimensionPixelSize(
                    R.dimen.lb_playback_controls_child_margin_biggest);
        }
        return sChildMarginBiggest;
    }
}
