package tv.mycujoo.domain.params

data class EventListParams(
    val pageSize: Int? = null,
    val pageToken: String? = null,
    val status: List<String>? = null,
    val orderBy: String? = null
) {
}