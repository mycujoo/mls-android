package tv.mycujoo.mls.widgets

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_event_info_pre_event_layout.view.*
import tv.mycujoo.mls.R

/**
 * Warning!
 * This view should only be created & used programmatically. Not suitable for inflating from Xml layout.
 */
@SuppressLint("ViewConstructor")
class CustomInformationDialog(
    container: ViewGroup,
    uiEvent: UiEvent,
    message: String
) :
    FrameLayout(container.context, null) {
    init {
        LayoutInflater.from(context)
            .inflate(R.layout.dialog_event_info_pre_event_layout, this, true)
        container.addView(this)

        eventInfoPreEventDialog_canvasView.visibility = VISIBLE
        eventInfoPreEventDialog_posterView.visibility = GONE

        eventInfoPreEventDialog_titleTextView.text = uiEvent.title ?: ""
        informationDialog_bodyTextView.text = message
        informationDialog_bodyTextView.setTextColor(Color.RED)
        informationDialog_dateTimeTextView.visibility = GONE
    }
}