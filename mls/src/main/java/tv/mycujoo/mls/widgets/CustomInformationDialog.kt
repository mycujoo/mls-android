package tv.mycujoo.mls.widgets

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_event_info_pre_event_layout.view.*
import kotlinx.android.synthetic.main.player_view_wrapper.view.*
import tv.mycujoo.mls.R

/**
 * Warning!
 * This view should only be created & used programmatically. Not suitable for inflating from Xml layout.
 */
@SuppressLint("ViewConstructor")
class CustomInformationDialog(
    mlsPlayerView: MLSPlayerView,
    uiEvent: UiEvent,
    message: String
) :
    FrameLayout(mlsPlayerView.context, null) {
    init {
        LayoutInflater.from(context)
            .inflate(R.layout.dialog_event_info_pre_event_layout, this, true)
        mlsPlayerView.infoDialogContainerLayout.addView(this)

        eventInfoPreEventDialog_canvasView.visibility = VISIBLE
        eventInfoPreEventDialog_posterView.visibility = GONE

        eventInfoPreEventDialog_titleTextView.text = uiEvent.title ?: ""
        informationDialog_bodyTextView.text = message
        informationDialog_bodyTextView.setTextColor(Color.RED)
        informationDialog_dateTimeTextView.visibility = GONE
    }
}