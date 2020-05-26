package tv.mycujoo.mls.widgets.overlay

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_overlay_scoreboard.view.*
import tv.mycujoo.mls.R
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction

class ScoreboardOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_overlay_scoreboard, this, true)
    }

    fun viewAction(action: ShowScoreboardOverlayAction) {
        scoreboardOverlayView_abbrLeftTextView.setBackgroundColor(Color.parseColor(action.colorLeft))
        scoreboardOverlayView_abbrRightTextView.setBackgroundColor(Color.parseColor(action.colorRight))

        scoreboardOverlayView_abbrLeftTextView.text = action.abbrLeft
        scoreboardOverlayView_abbrRightTextView.text = action.abbrRight

        scoreboardOverlayView_scoreLeftTextView.text = action.scoreLeft
        scoreboardOverlayView_abbrRightTextView.text = action.scoreRight
    }

}