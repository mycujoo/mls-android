package tv.mycujoo.data.entity

import com.google.gson.annotations.SerializedName

data class ActionSourceData(
    @SerializedName("id") var id: String,
    @SerializedName("type") var type: String,
    @SerializedName("data") var data: List<Pair<String, Any>>
)