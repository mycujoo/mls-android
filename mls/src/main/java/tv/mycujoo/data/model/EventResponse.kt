package tv.mycujoo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EventSourceData(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "description") val description: String,
    @field:Json(name = "thumbnail_url") val thumbnailUrl: String,
    @field:Json(name = "poster_url") val poster_url: String?,
    @field:Json(name = "location") val locationSourceData: LocationSourceData,
    @field:Json(name = "organiser") val organiser: String,
    @field:Json(name = "start_time") val start_time: String,
    @field:Json(name = "status") val status: String,
    @field:Json(name = "streams") val streams: List<StreamSourceData>,
    @field:Json(name = "timezone") val timezone: String,
    @field:Json(name = "timeline_ids") val timeline_ids: List<String>,
    @field:Json(name = "metadata") val metadata: MetadataSourceData,
    @field:Json(name = "is_test") val is_test: Boolean,
    @field:Json(name = "is_protected") val is_protected: Boolean,
)

@JsonClass(generateAdapter = true)
data class StreamSourceData(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "dvr_window_size") val dvrWindowString: String,
    @field:Json(name = "full_url") val fullUrl: String?,
    @field:Json(name = "drm") val drm: DrmSourceData?,
    @field:Json(name = "error") val errorCodeAndMessage: ErrorCodeAndMessageSourceData? = null
)

@JsonClass(generateAdapter = true)
data class DrmSourceData(
    @field:Json(name = "widevine") val widevine: WidevineSourceData?,
)

@JsonClass(generateAdapter = true)
data class WidevineSourceData(
    @field:Json(name = "full_url") val fullUrl: String?,
    @field:Json(name = "license_url") val licenseUrl: String?
)

@JsonClass(generateAdapter = true)
data class ErrorCodeAndMessageSourceData(
    @field:Json(name = "code") val code: String?,
    @field:Json(name = "message") val message: String?
)

@JsonClass(generateAdapter = true)
data class LocationSourceData(
    @field:Json(name = "physical") val physicalSourceData: PhysicalSourceData
)

@JsonClass(generateAdapter = true)
class MetadataSourceData

@JsonClass(generateAdapter = true)
data class PhysicalSourceData(
    @field:Json(name = "city") val city: String,
    @field:Json(name = "continent_code") val continent_code: String,
    @field:Json(name = "coordinates") val coordinates: CoordinatesSourceData,
    @field:Json(name = "country_code") val country_code: String,
    @field:Json(name = "venue") val venue: String
)

@JsonClass(generateAdapter = true)
data class CoordinatesSourceData(
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double
)