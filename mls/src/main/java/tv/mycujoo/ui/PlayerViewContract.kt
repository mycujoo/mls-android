package tv.mycujoo.ui

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.domain.entity.TimelineMarkerEntity

interface PlayerViewContract {
    fun context(): Context
    fun overlayHost(): ConstraintLayout
    fun setTimelineMarker(timelineMarkerEntityList: List<TimelineMarkerEntity>)
    fun clearScreen(idList: List<String>)
    fun updateTime(currentPosition: Long, duration: Long)
    fun setOnSizeChangedCallback(onSizeChangedCallback: () -> Unit)
}