package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class AnnotationSourceData(
    @SerializedName("id") val id: String?,
    @SerializedName("offset") var offset: Long?,
    @SerializedName("timeline_id") var timeline_id: String?,
    @SerializedName("actions") var actions: List<ActionSourceData?>?
)