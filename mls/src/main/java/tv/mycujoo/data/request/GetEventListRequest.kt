package tv.mycujoo.data.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetEventListRequest(
    @Json(name = "orderBy") val orderBy: String? = null,
    @Json(name = "pageSize") val pageSize: Int? = null,
    @Json(name = "pageToken") val pageToken: String? = null,
    @Json(name = "search") val search: String? = null,
    @Json(name = "filter") val filter: String? = null
)
