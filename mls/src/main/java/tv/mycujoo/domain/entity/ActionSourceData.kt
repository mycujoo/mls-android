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
            DataMapper.extractOverlayRelatedData(data),
            DataMapper.extractTimerRelatedData(data),
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
                val relatedData = DataMapper.extractOverlayRelatedData(data)
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
                val relatedData = DataMapper.extractOverlayRelatedData(data)
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
                val relatedData = DataMapper.extractTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.CreateTimerAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        name = relatedData.name,
                        format = relatedData.format,
                        direction = relatedData.direction,
                        startValue = relatedData.startValue,
                        capValue = relatedData.capValue
                    )
                }
            }
            START_TIMER -> {
                val relatedData = DataMapper.extractTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.StartTimerAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        name = relatedData.name
                    )
                }
            }
            PAUSE_TIMER -> {
                val relatedData = DataMapper.extractTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.PauseTimerAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        name = relatedData.name
                    )
                }
            }
            ADJUST_TIMER -> {
                val relatedData = DataMapper.extractTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.AdjustTimerAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        name = relatedData.name,
                        value = relatedData.value
                    )

                }
            }
            SKIP_TIMER -> {
                val relatedData = DataMapper.extractTimerRelatedData(data)
                if (relatedData != null) {
                    return Action.SkipTimerAction(
                        id = newId,
                        offset = newOffset,
                        absoluteTime = newAbsoluteTime,
                        name = relatedData.name,
                        value = relatedData.value
                    )
                }
            }

            SET_VARIABLE -> {
                val variable = DataMapper.mapToVariable(data)
                return Action.CreateVariableAction(
                    id = newId,
                    offset = newOffset,
                    absoluteTime = newAbsoluteTime,
                    variable = variable
                )

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