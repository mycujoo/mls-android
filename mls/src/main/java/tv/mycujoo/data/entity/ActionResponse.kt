package tv.mycujoo.data.entity


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import tv.mycujoo.domain.entity.ActionSourceData

@JsonClass(generateAdapter = true)
data class ActionResponse(
    @Json(name = "actions") var data: List<ActionSourceData>,
    @Json(name = "update_id") var updateId : String? = null
)