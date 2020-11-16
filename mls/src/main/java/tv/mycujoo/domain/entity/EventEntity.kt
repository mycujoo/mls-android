package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class EventEntity(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnail_url") val thumbnail_url: String,
    @SerializedName("poster_url") val poster_url: String?,
    @SerializedName("location") val location: Location,
    @SerializedName("organiser") val organiser: String,
    @SerializedName("start_time") val start_time: String,
    @SerializedName("status") val status: EventStatus,
    @SerializedName("streams") val streams: List<Stream>,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timeline_ids") val timeline_ids: List<String>,
    @SerializedName("metadata") val metadata: Metadata,
    @SerializedName("is_test") val is_test: Boolean
)

data class Stream(
    @SerializedName("id") val id: String,
    @SerializedName("full_url") val fullUrl: String?,
    @SerializedName("widevine") val widevine: Widevine?
)

data class Widevine(
    @SerializedName("full_url") val fullUrl: String?,
    @SerializedName("license_url") val licenseUrl: String?
)

data class Location(
    @SerializedName("physical") val physical: Physical
)

class Metadata(
)

data class Physical(
    @SerializedName("city") val city: String,
    @SerializedName("continent_code") val continent_code: String,
    @SerializedName("coordinates") val coordinates: Coordinates,
    @SerializedName("country_code") val country_code: String,
    @SerializedName("venue") val venue: String
)

data class Coordinates(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)