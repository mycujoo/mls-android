package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class Events(
    @SerializedName("events") val events: List<EventEntity>,
    @SerializedName("previous_page_token") val previousPageToken: String?,
    @SerializedName("next_page_token") val nextPageToken: String?
)
