package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class EventEntity(
    @SerializedName("id") val id: String,
    @SerializedName("description") val description: String,
    @SerializedName("is_test") val is_test: Boolean,
    @SerializedName("location") val location: Location,
    @SerializedName("metadata") val metadata: Metadata,
    @SerializedName("organiser") val organiser: String,
    @SerializedName("start_time") val start_time: String,
    @SerializedName("status") val status: String,
    @SerializedName("streams") val streams: List<Any>,
    @SerializedName("thumbnail_url") val thumbnail_url: String,
    @SerializedName("timeline_ids") val timeline_ids: List<Any>,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("title") val title: String
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