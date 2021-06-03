package tv.mycujoo.domain.entity

/**
 * Timeline marker info to mark some point on timeline based on Offset.
 * @property id identifier of action
 * @property offset offset at which marker should display on timeline
 * @property seekOffset amount of seekable portion around timeline marker
 * @property label marker text
 * @property color marker color
 */
data class TimelineMarkerEntity(
    val id: String,
    val offset: Long,
    val seekOffset: Long,
    val label: String,
    val color: String
)