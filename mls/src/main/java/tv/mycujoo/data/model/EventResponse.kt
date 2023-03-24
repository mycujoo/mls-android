package tv.mycujoo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventResponse(
    @field:Json(name = "event") val event: EventSourceData
)

@JsonClass(generateAdapter = true)
data class EventSourceData(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "description") val description: String,
    @field:Json(name = "thumbnailUrl") val thumbnailUrl: String?,
    @field:Json(name = "posterUrl") val poster_url: String?,
    @field:Json(name = "physical") val physical: PhysicalSourceData,
    @field:Json(name = "organiser") val organiser: String?,
    @field:Json(name = "startTime") val start_time: String,
    @field:Json(name = "status") val status: String,
    @field:Json(name = "streams") val streams: List<StreamSourceData> = emptyList(),
    @field:Json(name = "timezone") val timezone: String,
    @field:Json(name = "timelineIds") val timeline_ids: List<String> = emptyList(),
    @field:Json(name = "metadata") val metadata: MetadataSourceData,
    @field:Json(name = "isTest") val is_test: Boolean = false,
    @field:Json(name = "isProtected") val is_protected: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class StreamSourceData(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "dvrWindowSize") val dvrWindowString: String?,
    @field:Json(name = "fullUrl") val fullUrl: String?,
    @field:Json(name = "drm") val drm: DrmSourceData?,
    @field:Json(name = "error") val errorCodeAndMessage: ErrorCodeAndMessageSourceData? = null
)

@JsonClass(generateAdapter = true)
data class DrmSourceData(
    @field:Json(name = "widevine") val widevine: WidevineSourceData?,
)

@JsonClass(generateAdapter = true)
data class WidevineSourceData(
    @field:Json(name = "fullUrl") val fullUrl: String?,
    @field:Json(name = "licenseUrl") val licenseUrl: String?
)

@JsonClass(generateAdapter = true)
data class ErrorCodeAndMessageSourceData(
    @field:Json(name = "code") val code: String?,
    @field:Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
class MetadataSourceData

@JsonClass(generateAdapter = true)
data class PhysicalSourceData(
    @field:Json(name = "city") val city: String,
    @field:Json(name = "continentCode") val continent_code: String,
    @field:Json(name = "coordinates") val coordinates: CoordinatesSourceData,
    @field:Json(name = "countryCode") val country_code: String,
    @field:Json(name = "venue") val venue: String
)

@JsonClass(generateAdapter = true)
data class CoordinatesSourceData(
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double
)