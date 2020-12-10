package tv.mycujoo.mls.widgets

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.gms.cast.framework.CastButtonFactory
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.mlstimebar.MLSTimeBar

class RemotePlayerControllerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var listener: RemotePlayerControllerListener? = null
    private val timeBar: MLSTimeBar
    private val playButtonContainer: FrameLayout
    private val playButton: ImageButton
    private val pauseButton: ImageButton
    private val fastForwardContainer: FrameLayout
    private val fastForwardButton: ImageButton
    private val rewindContainer: FrameLayout
    private val rewindButton: ImageButton

    private val mediaRouteButton: MediaRouteButton


    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.view_remote_player_controller, this, true)
        playButtonContainer = findViewById(R.id.remoteControllerPlayPauseButtonContainerLayout)
        playButton = findViewById(R.id.remoteControllerPlay)
        pauseButton = findViewById(R.id.remoteControllerPause)
        fastForwardContainer = findViewById(R.id.remoteControllerFastForwardButtonContainerLayout)
        fastForwardButton = findViewById(R.id.remoteControllerFastForwardButton)
        rewindContainer = findViewById(R.id.remoteControllerRewButtonContainerLayout)
        rewindButton = findViewById(R.id.remoteControllerRewindButton)
        timeBar = findViewById(R.id.timeBar)
        initButtonsListener()
        initTimeBarListener()


        mediaRouteButton = findViewById(R.id.mediaRouteButton)
        CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton)
    }

    private fun initButtonsListener() {
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
    /**endregion */

    /**region Controls*/
    fun setDuration(duration: Long) {
        timeBar.setDuration(duration)
    }

    fun setPosition(position: Long) {
        timeBar.setPosition(position)
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


    /**endregion */
}