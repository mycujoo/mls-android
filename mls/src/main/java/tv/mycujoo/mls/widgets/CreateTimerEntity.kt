package tv.mycujoo.mls.widgets

import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat

class CreateTimerEntity(
    var name: String,
    var offset: Long,
    var format: ScreenTimerFormat,
    var direction: ScreenTimerDirection,
    val startValue: Long,
    var step: Long,
    var capValue: Long
) {
}