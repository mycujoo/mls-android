package tv.mycujoo.mcls.tv.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.leanback.widget.PlaybackControlsRow
import androidx.leanback.widget.PlaybackControlsRow.PlayPauseAction
import tv.mycujoo.mcls.R

class MLSPlayPauseAction(context: Context, primaryColor: String) :
    PlaybackControlsRow.MultiAction(R.id.lb_control_play_pause) {
    init {
        val drawables = arrayOfNulls<Drawable>(2)
        val playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play)
        DrawableCompat.setTint(playDrawable!!, Color.parseColor(primaryColor))
        drawables[PlayPauseAction.INDEX_PLAY] =
            playDrawable
        val pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause)
        DrawableCompat.setTint(pauseDrawable!!, Color.parseColor(primaryColor))
        drawables[PlayPauseAction.INDEX_PAUSE] =
            pauseDrawable

        setDrawables(drawables)

        val labels = arrayOfNulls<String>(drawables.size)
        labels[PlayPauseAction.INDEX_PLAY] = context.getString(R.string.lb_playback_controls_play)
        labels[PlayPauseAction.INDEX_PAUSE] = context.getString(R.string.lb_playback_controls_pause)
        setLabels(labels)
        addKeyCode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        addKeyCode(KeyEvent.KEYCODE_MEDIA_PLAY)
        addKeyCode(KeyEvent.KEYCODE_MEDIA_PAUSE)
    }
}