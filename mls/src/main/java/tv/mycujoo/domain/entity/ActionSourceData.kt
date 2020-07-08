package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName

data class ActionSourceData(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("data") val data: Map<String, Any>?
)