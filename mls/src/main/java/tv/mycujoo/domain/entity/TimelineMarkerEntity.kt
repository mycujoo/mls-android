package tv.mycujoo.domain.entity

data class TimelineMarkerEntity(
    val id: String,
    val offset: Long,
    val label: String,
    val color: String
)