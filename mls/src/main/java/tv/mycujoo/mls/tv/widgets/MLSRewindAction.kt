package tv.mycujoo.mls.tv.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.leanback.widget.PlaybackControlsRow
import tv.mycujoo.mls.R

class MLSRewindAction(
    context: Context,
    numOfSpeed: Int = 1,
    primaryColor: String
) :
    PlaybackControlsRow.MultiAction(R.id.lb_control_fast_rewind) {

    init {
        require(numOfSpeed >= 1) { "numSpeeds must be > 0" }
        val drawables = arrayOfNulls<Drawable>(numOfSpeed + 1)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_10_sec_rewind)
        DrawableCompat.setTint(drawable!!, Color.parseColor(primaryColor))
        drawables[0] = drawable
        setDrawables(drawables)

        val labels = arrayOfNulls<String>(actionCount)
        labels[0] = context.getString(androidx.leanback.R.string.lb_playback_controls_rewind)

        val labels2 = arrayOfNulls<String>(actionCount)
        labels2[0] = labels[0]

        for (i in 1..numOfSpeed) {
            val multiplier = i + 1
            labels[i] = context.resources.getString(
                androidx.leanback.R.string.lb_control_display_rewind_multiplier, multiplier
            )
            labels[i] = labels[i]
            labels2[i] = context.resources.getString(
                androidx.leanback.R.string.lb_playback_controls_rewind_multiplier, multiplier
            )
        }
        setLabels(labels)
        setSecondaryLabels(labels2)
        addKeyCode(KeyEvent.KEYCODE_MEDIA_REWIND)
    }
}