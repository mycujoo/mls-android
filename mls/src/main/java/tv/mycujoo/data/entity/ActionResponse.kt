package tv.mycujoo.data.entity


import com.squareup.moshi.Json
import tv.mycujoo.domain.entity.ActionSourceData

data class ActionResponse(
    @field:Json(name = "actions") var data: List<ActionSourceData>,
    @field:Json(name = "update_id") var updateId : String? = null
)