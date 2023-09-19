package tv.mycujoo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventResponse(
    @Json(name = "event") val event: EventSourceData
)

@JsonClass(generateAdapter = true)
data class EventSourceData(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String = "",
    @Json(name = "thumbnailUrl") val thumbnailUrl: String?,
    @Json(name = "posterUrl") val poster_url: String?,
    @Json(name = "physical") val physical: PhysicalSourceData?,
    @Json(name = "organiser") val organiser: String?,
    @Json(name = "startTime") val start_time: String,
    @Json(name = "status") val status: String = "",
    @Json(name = "streams") val streams: List<StreamSourceData> = emptyList(),
    @Json(name = "timezone") val timezone: String = "",
    @Json(name = "timelineIds") val timeline_ids: List<String> = emptyList(),
    @Json(name = "metadata") val metadata: MetadataSourceData?,
    @Json(name = "isTest") val is_test: Boolean = false,
    @Json(name = "isProtected") val is_protected: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class StreamSourceData(
    @Json(name = "id") val id: String,
    @Json(name = "dvrWindowSize") val dvrWindowString: String?,
    @Json(name = "fullUrl") val fullUrl: String?,
    @Json(name = "drm") val drm: DrmSourceData?,
    @Json(name = "error") val errorCodeAndMessage: ErrorCodeAndMessageSourceData? = null
)

@JsonClass(generateAdapter = true)
data class DrmSourceData(
    @Json(name = "widevine") val widevine: WidevineSourceData?,
)

@JsonClass(generateAdapter = true)
data class WidevineSourceData(
    @Json(name = "fullUrl") val fullUrl: String?,
    @Json(name = "licenseUrl") val licenseUrl: String?
)

@JsonClass(generateAdapter = true)
data class ErrorCodeAndMessageSourceData(
    @Json(name = "code") val code: String?,
    @Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
class MetadataSourceData

@JsonClass(generateAdapter = true)
data class PhysicalSourceData(
    @Json(name = "city") val city: String = "",
    @Json(name = "continentCode") val continent_code: String = "",
    @Json(name = "coordinates") val coordinates: CoordinatesSourceData? = null,
    @Json(name = "countryCode") val country_code: String = "",
    @Json(name = "venue") val venue: String = ""
)

@JsonClass(generateAdapter = true)
data class CoordinatesSourceData(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)