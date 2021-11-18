package tv.mycujoo.mcls.widgets

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import kotlinx.android.synthetic.main.dialog_event_info_pre_event_layout.view.*
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

        container.findViewById<LinearLayout>(R.id.preEventInfoDialog_textualLayout).visibility =
            VISIBLE
        container.findViewById<AppCompatImageView>(R.id.eventInfoPreEventDialog_posterView).visibility =
            GONE

        container.findViewById<TextView>(R.id.preEventInfoDialog_titleTextView).text =
            uiEvent.title ?: ""
        container.findViewById<TextView>(R.id.preEventInfoDialog_bodyTextView).text = message
        container.findViewById<TextView>(R.id.preEventInfoDialog_bodyTextView)
            .setTextColor(Color.RED)
        container.findViewById<TextView>(R.id.preEventInfoDialog_startTimeTextView).visibility =
            GONE
    }
}