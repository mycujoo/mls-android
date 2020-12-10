package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
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

    private val mediaRouteButton: MediaRouteButton


    /**region Initializing*/
    init {
        LayoutInflater.from(context).inflate(R.layout.view_remote_player_controller, this, true)
        timeBar = findViewById(R.id.timeBar)
        initTimeBarListener()


        mediaRouteButton = findViewById(R.id.mediaRouteButton)
        CastButtonFactory.setUpMediaRouteButton(context, mediaRouteButton)
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


    /**endregion */
}