package tv.mycujoo.mls.helper

import android.view.View
import android.widget.TextView
import tv.mycujoo.mls.entity.TimeLineAction
import tv.mycujoo.mls.entity.TimeLineItem

class TimeBarAnnotationHelper(val timeLineItemList: List<TimeLineItem>) {
    fun updateText(currentPosition: Long, previewTitleTextView: TextView) {

        var shouldBeVisible = false

        timeLineItemList.forEachIndexed { index, timeLineItem ->

            if (TimeRangeHelper.isInRange(currentPosition, timeLineItem.streamOffset)) {
                shouldBeVisible = true
                previewTitleTextView.text = (timeLineItem.action as TimeLineAction).text
            }
        }

        when (shouldBeVisible) {
            true -> {
                previewTitleTextView.visibility = View.VISIBLE
            }
            false -> {
                previewTitleTextView.visibility = View.GONE

            }
        }
    }

}
