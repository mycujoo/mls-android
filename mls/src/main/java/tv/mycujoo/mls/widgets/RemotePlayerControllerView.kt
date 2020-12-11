package tv.mycujoo.mls.widgets

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.gms.cast.framework.CastButtonFactory
import tv.mycujoo.mls.R
import tv.mycujoo.mls.utils.StringUtils
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar
import java.util.*

class RemotePlayerControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {


    /**region Fields*/
    var listener: RemotePlayerControllerListener? = null
    private val bufferingProgressBar: ProgressBar
    private val playButtonContainer: FrameLayout
    private val playButton: ImageButton
    private val pauseButton: ImageButton
    private val fastForwardContainer: FrameLayout
    private val fastForwardButton: ImageButton
    private val rewindContainer: FrameLayout
    private val rewindButton: ImageButton
    private val timeBar: MLSTimeBar
    private val currentPositionTextView: TextView
    private val durationTextView: TextView

    private val liveBadgeView: LiveBadgeView
    private val mediaRouteButton: MediaRouteButton

    private var timeFormatBuilder = StringBuilder()
    private var timeFormatter = Formatter(timeFormatBuilder, Locale.getDefault())
    /**endregion */


    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.view_remote_player_controller, this, true)
        bufferingProgressBar = findViewById(R.id.remoteControllerBufferingProgressBar)
        playButtonContainer = findViewById(R.id.remoteControllerPlayPauseButtonContainerLayout)
        playButton = findViewById(R.id.remoteControllerPlay)
        pauseButton = findViewById(R.id.remoteControllerPause)
        fastForwardContainer = findViewById(R.id.remoteControllerFastForwardButtonContainerLayout)
        fastForwardButton = findViewById(R.id.remoteControllerFastForwardButton)
        rewindContainer = findViewById(R.id.remoteControllerRewButtonContainerLayout)
        rewindButton = findViewById(R.id.remoteControllerRewindButton)
        timeBar = findViewById(R.id.timeBar)
        currentPositionTextView = findViewById(R.id.remoteControllerCurrentPositionTextView)
        durationTextView = findViewById(R.id.remoteControllerDurationTextView)
        initButtonsListener()
        initTimeBarListener()


        liveBadgeView = findViewById(R.id.remoteControllerLiveBadgeView)
        mediaRouteButton = findViewById(R.id.mediaRouteButton)
        CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton)
    }

    private fun initButtonsListener() {
        playButton.setOnClickListener { listener?.onPlay() }
        pauseButton.setOnClickListener { listener?.onPause() }

        val fastForward = { listener?.onFastForward(10000L) }
        fastForwardContainer.setOnClickListener { fastForward.invoke() }
        fastForwardButton.setOnClickListener { fastForward.invoke() }

        val rewind = { listener?.onSeekTo(-10000L) }
        rewindContainer.setOnClickListener { rewind.invoke() }
        rewindButton.setOnClickListener { rewind.invoke() }
    }

    private fun initTimeBarListener() {
        timeBar.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                listener?.onSeekTo(position)
            }
        })
    }

    /**region Controls*/
    fun setPlayStatus(isPlaying: Boolean, isBuffering: Boolean? = null) {
        when (isBuffering) {
            true -> {
                bufferingProgressBar.visibility = View.VISIBLE
            }
            else -> {
                bufferingProgressBar.visibility = View.GONE
            }
        }

        when (isPlaying) {
            true -> {
                playButton.visibility = View.GONE
                pauseButton.visibility = View.VISIBLE
            }
            false -> {
                if (isBuffering == true) {
                    return
                }
                playButton.visibility = View.VISIBLE
                pauseButton.visibility = View.GONE
            }
        }
    }

    fun setDuration(duration: Long) {
        timeBar.setDuration(duration)
        durationTextView.text =
            StringUtils.getFormattedTime(duration, timeFormatBuilder, timeFormatter)
    }

    fun setPosition(position: Long) {
        timeBar.setPosition(position)
        currentPositionTextView.text =
            StringUtils.getFormattedTime(position, timeFormatBuilder, timeFormatter)

    }

    fun setTimeBarPlayedColor(@ColorInt color: Int) {
        timeBar.setPlayedColor(color)
    }

    fun setPlayerMainButtonsColor(@ColorInt primaryColor: Int) {
        playButton.setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )
        pauseButton.setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )
        fastForwardButton.setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )
        rewindButton.setColorFilter(
            primaryColor,
            PorterDuff.Mode.SRC_ATOP
        )
    }

    fun setLiveMode(liveState: MLSPlayerView.LiveState) {
        liveBadgeView.setLiveMode(liveState)
    }


    /**endregion */
}