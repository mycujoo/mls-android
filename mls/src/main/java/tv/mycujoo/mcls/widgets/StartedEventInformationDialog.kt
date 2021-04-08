package tv.mycujoo.mcls.widgets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.dialog_event_info_started_layout.view.*
import kotlinx.android.synthetic.main.player_view_wrapper.view.*
import tv.mycujoo.mcls.R

@SuppressLint("ViewConstructor")
class StartedEventInformationDialog(mlsPlayerView: MLSPlayerView, uiEvent: UiEvent) :
    FrameLayout(mlsPlayerView.context, null) {

    init {
        val dialog =
            LayoutInflater.from(context)
                .inflate(R.layout.dialog_event_info_started_layout, this, true)
        mlsPlayerView.infoDialogContainerLayout.addView(dialog)

        dialog.startedEventInfoDialog_titleTextView.text = uiEvent.title ?: ""
        dialog.startedEventInfoDialog_bodyTextView.text =
            uiEvent.description ?: ""
        dialog.startedEventInfoDialog_startTimeTextView.text = uiEvent.startTime
        dialog.setOnClickListener { mlsPlayerView.hideInfoDialogs() }
    }
}