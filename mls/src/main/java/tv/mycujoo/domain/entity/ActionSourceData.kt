package tv.mycujoo.domain.entity

import com.google.gson.annotations.SerializedName
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ActionType.*
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


    fun toAction(): Action {
        val newId = id.orEmpty()
        val newType = ActionType.fromValueOrUnknown(this.type.orEmpty())
        var newOffset: Long = -1L
        offset?.let { newOffset = it }
        var newAbsoluteTime: Long = -1L
        absoluteTime?.let { newAbsoluteTime = it }


        when (newType) {
            SHOW_OVERLAY -> {
                val relatedData = DataMapper.parseOverlayRelatedData(data)
                if (relatedData != null) {
                    return Action.ShowOverlayAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        duration = relatedData.duration,
                        viewSpec = ViewSpec(
                            relatedData.positionGuide,
                            relatedData.sizePair
                        ),
                        svgData = SvgData(relatedData.svgUrl),
                        introAnimationSpec = TransitionSpec(
                            newOffset,
                            relatedData.introAnimationType,
                            relatedData.introAnimationDuration
                        ),
                        outroAnimationSpec = null, // todo
                        placeHolders = relatedData.variablePlaceHolders
                    )
                }
            }
            HIDE_OVERLAY -> {
                val relatedData = DataMapper.parseOverlayRelatedData(data)
                if (relatedData != null) {
                    return Action.HideOverlayAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        outroAnimationSpec = TransitionSpec(
                            newOffset,
                            relatedData.outroAnimationType,
                            relatedData.outroAnimationDuration
                        ),
                        customId = relatedData.id
                    )

                }
            }

            CREATE_TIMER -> {
                val relatedData = DataMapper.parseTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.CreateTimerAction(
                        newId,
                        newOffset,
                        newAbsoluteTime,
                        relatedData.name,
                        relatedData.format,
                        relatedData.direction,
                        relatedData.startValue,
                        relatedData.capValue
                    )
                }
            }
            START_TIMER -> {
                val relatedData = DataMapper.parseTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.StartTimerAction(
                        newId,
                        newOffset,
                        newAbsoluteTime,
                        relatedData.name
                    )
                }
            }
            PAUSE_TIMER -> {
                return Action.PauseTimerAction(newId, newOffset, newAbsoluteTime)
            }
            ADJUST_TIMER -> {
                return Action.AdjustTimerAction(newId, newOffset, newAbsoluteTime)
            }
            SKIP_TIMER -> {
                return Action.SkipTimerAction(newId, newOffset, newAbsoluteTime)
            }

            SET_VARIABLE -> {
                return Action.CreateVariableAction(newId, newOffset, newAbsoluteTime)
            }
            INCREMENT_VARIABLE -> {
                return Action.IncrementVariableAction(newId, newOffset, newAbsoluteTime)
            }

            SHOW_TIMELINE_MARKER -> {
                return Action.MarkTimelineAction(newId, newOffset, newAbsoluteTime)
            }

            DELETE_ACTION -> {
                return Action.DeleteAction(newId, newOffset, newAbsoluteTime)
            }
            UNKNOWN -> {
                // do nothing, returns InvalidAction
            }
        }
        return Action.InvalidAction(newId, newOffset, newAbsoluteTime)
    }
}