package tv.mycujoo.mcls.tv.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.leanback.widget.PlaybackControlsRow
import tv.mycujoo.mcls.R

class MLSFastForwardAction(
    context: Context,
    numOfSpeed: Int = 1,
    primaryColor: String
) :
    PlaybackControlsRow.MultiAction(R.id.lb_control_fast_forward) {

    init {
        require(numOfSpeed >= 1) { "numSpeeds must be > 0" }
        val drawables = arrayOfNulls<Drawable>(numOfSpeed + 1)
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_10_sec_forward)
        DrawableCompat.setTint(drawable!!, Color.parseColor(primaryColor))
        drawables[0] = drawable
        setDrawables(drawables)

        val labels = arrayOfNulls<String>(actionCount)
        labels[0] = context.getString(R.string.lb_playback_controls_fast_forward)

        val labels2 = arrayOfNulls<String>(actionCount)
        labels2[0] = labels[0]

        for (i in 1..numOfSpeed) {
            val multiplier = i + 1
            labels[i] = context.resources.getString(
                R.string.lb_control_display_fast_forward_multiplier, multiplier
            )
            labels2[i] = context.resources.getString(
                R.string.lb_playback_controls_fast_forward_multiplier, multiplier
            )
        }
        setLabels(labels)
        setSecondaryLabels(labels2)
        addKeyCode(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD)
    }

}