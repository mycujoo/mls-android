package tv.mycujoo.mcls.widgets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.dialog_event_info_pre_event_layout.view.*
import tv.mycujoo.mcls.R

@SuppressLint("ViewConstructor")
class PreEventInformationDialog(container: ViewGroup, uiEvent: UiEvent) :
    FrameLayout(container.context, null) {
    init {
        val dialog =
            LayoutInflater.from(container.context)
                .inflate(R.layout.dialog_event_info_pre_event_layout, this, true)
        container.addView(this)

        if (uiEvent.posterUrl != null && uiEvent.posterUrl.isNotEmpty()) {
            dialog.eventInfoPreEventDialog_posterView.visibility = View.VISIBLE
            dialog.preEventInfoDialog_textualLayout.visibility = View.GONE

            Glide.with(dialog.eventInfoPreEventDialog_posterView)
                .load(uiEvent.posterUrl)
                .into(dialog.eventInfoPreEventDialog_posterView as ImageView)
        } else {
            dialog.preEventInfoDialog_textualLayout.visibility = View.VISIBLE
            dialog.eventInfoPreEventDialog_posterView.visibility = View.GONE

            dialog.preEventInfoDialog_titleTextView.text = uiEvent.title ?: ""
            dialog.preEventInfoDialog_bodyTextView.text = uiEvent.description ?: ""
            dialog.preEventInfoDialog_startTimeTextView.text = uiEvent.startTime
        }
    }
}