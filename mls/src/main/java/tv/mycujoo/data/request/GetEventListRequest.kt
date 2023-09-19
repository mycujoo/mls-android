package tv.mycujoo.data.request

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetEventListRequest(
    @field:Json(name = "orderBy") val orderBy: String? = null,
    @field:Json(name = "pageSize") val pageSize: Int? = null,
    @field:Json(name = "pageToken") val pageToken: String? = null,
    @field:Json(name = "search") val search: String? = null,
    @field:Json(name = "filter") val filter: String? = null
)