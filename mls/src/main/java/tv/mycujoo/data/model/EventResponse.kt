package tv.mycujoo.data.model

import com.google.gson.annotations.SerializedName

data class EventSourceData(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String,
    @SerializedName("poster_url") val poster_url: String?,
    @SerializedName("location") val locationSourceData: LocationSourceData,
    @SerializedName("organiser") val organiser: String,
    @SerializedName("start_time") val start_time: String,
    @SerializedName("status") val status: String,
    @SerializedName("streams") val streams: List<StreamSourceData>,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timeline_ids") val timeline_ids: List<String>,
    @SerializedName("metadata") val metadata: MetadataSourceData,
    @SerializedName("is_test") val is_test: Boolean
)

data class StreamSourceData(
    @SerializedName("id") val id: String,
    @SerializedName("dvr_window_size") val dvrWindowString: String,
    @SerializedName("full_url") val fullUrl: String?,
    @SerializedName("widevine") val widevine: WidevineSourceData?,
    @SerializedName("error") val errorCodeAndMessage: ErrorCodeAndMessageSourceData? = null
)

data class WidevineSourceData(
    @SerializedName("full_url") val fullUrl: String?,
    @SerializedName("license_url") val licenseUrl: String?
)

data class ErrorCodeAndMessageSourceData(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?
)

data class LocationSourceData(
    @SerializedName("physical") val physicalSourceData: PhysicalSourceData
)

class MetadataSourceData(
)

data class PhysicalSourceData(
    @SerializedName("city") val city: String,
    @SerializedName("continent_code") val continent_code: String,
    @SerializedName("coordinates") val coordinates: CoordinatesSourceData,
    @SerializedName("country_code") val country_code: String,
    @SerializedName("venue") val venue: String
)

data class CoordinatesSourceData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)