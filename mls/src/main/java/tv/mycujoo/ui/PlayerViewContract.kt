package tv.mycujoo.ui

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import tv.mycujoo.domain.entity.TimelineMarkerEntity

interface PlayerViewContract {
    fun context(): Context
    fun overlayHost(): ConstraintLayout
    fun clearScreen(idList: List<String>)
}