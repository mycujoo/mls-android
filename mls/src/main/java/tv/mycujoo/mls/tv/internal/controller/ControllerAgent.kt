package tv.mycujoo.mls.tv.internal.controller

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.Player
import tv.mycujoo.mls.widgets.MLSPlayerView

/**
 * Represent possible actions for Controller [Live state, Viewers count]
 *
 * Use ILiveBadgeStateListener to change Live state of controller
 *
 * Use setViewerCountView() to change viewers count or hide it
 */
class ControllerAgent(val player: Player) {

    private lateinit var viewersCountTextView: TextView
    private lateinit var viewersCountLayout: ConstraintLayout
    private lateinit var badgeStateListener: ILiveBadgeStateListener

    fun addLiveBadgeStateListener(listenerBadge: ILiveBadgeStateListener) {
        this.badgeStateListener = listenerBadge
    }

    fun setControllerLiveMode(state: MLSPlayerView.LiveState) {
        if (this::badgeStateListener.isInitialized.not()) {
            return
        }

        badgeStateListener.setState(state)
    }

    fun backToLive() {
        if (player.duration > 0) {
            player.seekTo(player.duration)
        }
    }

    fun setViewerCountView(viewersCountLayout: ConstraintLayout, viewersCountTextView: TextView) {
        this.viewersCountLayout = viewersCountLayout
        this.viewersCountTextView = viewersCountTextView
    }

    fun setViewerCount(numberOfViewers: String) {
        if (this::viewersCountLayout.isInitialized.not() || this::viewersCountTextView.isInitialized.not()) {
            return
        }
        viewersCountLayout.post {
            viewersCountLayout.visibility = View.VISIBLE
            viewersCountTextView.text = numberOfViewers
        }


    }

    fun hideViewersCount() {
        if (this::viewersCountLayout.isInitialized.not() || this::viewersCountTextView.isInitialized.not()) {
            return
        }
        viewersCountLayout.post {
            viewersCountLayout.visibility = View.GONE
        }
    }


}