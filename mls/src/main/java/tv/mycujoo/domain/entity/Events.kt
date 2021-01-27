package tv.mycujoo.domain.entity

data class Events(
    val eventEntities: List<EventEntity>,
    val previousPageToken: String?,
    val nextPageToken: String?
)
