package tv.mycujoo.data.model

import com.google.gson.annotations.SerializedName

data class EventsSourceData(
    @SerializedName("events") val events: List<EventSourceData>,
    @SerializedName("previous_page_token") val previousPageToken: String?,
    @SerializedName("next_page_token") val nextPageToken: String?
)