package tv.mycujoo.mls.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import tv.mycujoo.mls.R
import tv.mycujoo.mls.widgets.MLSPlayerView.LiveState.*

class LiveBadgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        background = ContextCompat.getDrawable(context, R.drawable.bg_live)
        setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    fun setLiveMode(liveState: MLSPlayerView.LiveState) {
        when (liveState) {
            LIVE_ON_THE_EDGE -> {
                visibility = View.VISIBLE

                background =
                    ContextCompat.getDrawable(context, R.drawable.bg_live)
                isEnabled = false
            }
            LIVE_TRAILING -> {
                visibility = View.VISIBLE

                background =
                    ContextCompat.getDrawable(context, R.drawable.bg_live_gray)
                isEnabled = true
            }
            VOD -> {
                visibility = View.GONE
            }
        }
    }
}