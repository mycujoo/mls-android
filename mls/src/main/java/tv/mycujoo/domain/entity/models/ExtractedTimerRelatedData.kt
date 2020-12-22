package tv.mycujoo.domain.entity.models

import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat

data class ExtractedTimerRelatedData(
    val name: String,
    val format: ScreenTimerFormat,
    val direction: ScreenTimerDirection,
    val startValue: Long,
    val step: Long,
    val capValue: Long,
    val value: Long
) {
}