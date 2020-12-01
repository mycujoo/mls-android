package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.mapper.DataMapper

data class ActionSourceData(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("offset") val offset: Long?,
    @SerializedName("absoluteTime") val absoluteTime: Long?,
    @SerializedName("data") val data: Map<String, Any>?
) {
    fun toActionObject(): ActionObject {

        val newId = id.orEmpty()
        val newType = ActionType.fromValueOrUnknown(this.type.orEmpty())
        var newOffset: Long = -1L
        offset?.let { newOffset = it }
        var newAbsoluteTime: Long = -1L
        absoluteTime?.let { newAbsoluteTime = it }

        return ActionObject(
            newId,
            newType,
            newOffset,
            newAbsoluteTime,
            DataMapper.parseOverlayRelatedData(data),
            DataMapper.parseTimerRelatedData(data),
            data
        )
    }
}