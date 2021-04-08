package tv.mycujoo.domain.entity.models

import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat

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