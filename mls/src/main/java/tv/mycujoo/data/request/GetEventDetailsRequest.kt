package tv.mycujoo.data.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetEventDetailsRequest(
    @field:Json(name = "id") val eventId: String
)