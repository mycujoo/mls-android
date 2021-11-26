package tv.mycujoo.mcls.widgets

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import tv.mycujoo.mcls.R

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

        this.findViewById<View>(R.id.preEventInfoDialog_textualLayout).visibility = VISIBLE
        this.findViewById<View>(R.id.eventInfoPreEventDialog_posterView).visibility = GONE

        this.findViewById<TextView>(R.id.preEventInfoDialog_titleTextView).text = uiEvent.title ?: ""
        this.findViewById<TextView>(R.id.preEventInfoDialog_bodyTextView).text = message
        this.findViewById<TextView>(R.id.preEventInfoDialog_bodyTextView).setTextColor(Color.RED)
        this.findViewById<TextView>(R.id.preEventInfoDialog_startTimeTextView).visibility = GONE
    }
}