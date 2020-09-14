package tv.mycujoo.data.entity

import com.google.gson.annotations.SerializedName
import tv.mycujoo.domain.entity.ActionSourceData

data class ActionResponse(
    @SerializedName("actions") var data: List<ActionSourceData>,
    @SerializedName("update_id") var updateId : String? = null
)