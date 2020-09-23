package tv.mycujoo.mls.tv.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.leanback.widget.PlaybackControlsRow
import androidx.leanback.widget.PlaybackControlsRow.PlayPauseAction
import tv.mycujoo.mls.R

class MLSPlayPauseAction(context: Context) :
    PlaybackControlsRow.MultiAction(R.id.lb_control_play_pause) {
    init {
        val drawables = arrayOfNulls<Drawable>(2)
        drawables[PlayPauseAction.INDEX_PLAY] =
            ContextCompat.getDrawable(context, R.drawable.ic_play)
        drawables[PlayPauseAction.INDEX_PAUSE] =
            ContextCompat.getDrawable(context, R.drawable.ic_pause)

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