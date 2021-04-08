package tv.mycujoo.mcls.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.widgets.MLSPlayerView.LiveState.*

class LiveBadgeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        text = context.getString(R.string.live)
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