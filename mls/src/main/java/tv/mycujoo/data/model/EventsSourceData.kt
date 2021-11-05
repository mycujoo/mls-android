package tv.mycujoo.data.model

import com.squareup.moshi.Json

data class EventsSourceData(
    @field:Json(name = "events") val events: List<EventSourceData>,
    @field:Json(name = "previous_page_token") val previousPageToken: String?,
    @field:Json(name = "next_page_token") val nextPageToken: String?
)