package tv.mycujoo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventsSourceData(
    @Json(name = "events") val events: List<EventSourceData>,
    @Json(name = "previous_page_token") val previousPageToken: String?,
    @Json(name = "next_page_token") val nextPageToken: String?
)