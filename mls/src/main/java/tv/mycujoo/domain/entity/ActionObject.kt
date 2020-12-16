package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.VariableType.*
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData
import tv.mycujoo.domain.entity.models.ParsedTimerRelatedData
import tv.mycujoo.domain.mapper.ActionMapper.Companion.INVALID_FLOAT_VALUE
import tv.mycujoo.domain.mapper.ActionMapper.Companion.INVALID_LONG_VALUE
import tv.mycujoo.domain.mapper.ActionMapper.Companion.INVALID_STRING_VALUE
import tv.mycujoo.mls.entity.*

data class ActionObject(
    val id: String,
    val type: ActionType,
    val offset: Long,
    val absoluteTime: Long,
    val overlayRelatedData: ParsedOverlayRelatedData?,
    val timerRelatedData: ParsedTimerRelatedData?,
    val rawData: Map<String, Any>?
) {

    var priority: Int = 0

    init {
        priority = when (type) {
            DELETE_ACTION -> {
                2000
            }
            CREATE_TIMER,
            SET_VARIABLE -> {
                1000
            }
            START_TIMER -> {
                500
            }
            PAUSE_TIMER -> {
                400
            }
            ADJUST_TIMER -> {
                300
            }
            SKIP_TIMER,
            INCREMENT_VARIABLE,
            UNKNOWN,
            SHOW_OVERLAY,
            HIDE_OVERLAY,
            SHOW_TIMELINE_MARKER -> {
                0
            }
        }
    }

    /**region Overlay related*/
    fun toOverlayEntity(): OverlayEntity? {
        if (overlayRelatedData == null) {
            return null
        }
        val svgData = SvgData(
            overlayRelatedData.svgUrl,
            null
        )
        val viewSpec = ViewSpec(overlayRelatedData.positionGuide, overlayRelatedData.sizePair)
        val introTransitionSpec = TransitionSpec(
            offset,
            overlayRelatedData.introAnimationType,
            overlayRelatedData.introAnimationDuration
        )

        val outroTransitionSpec: TransitionSpec =
            if (overlayRelatedData.duration != null) {
                TransitionSpec(
                    offset + overlayRelatedData.duration,
                    if (overlayRelatedData.outroAnimationType == AnimationType.NONE) {
                        AnimationType.NONE
                    } else {
                        overlayRelatedData.outroAnimationType
                    },
                    overlayRelatedData.outroAnimationDuration
                )
            } else {
                TransitionSpec(
                    -1L,
                    AnimationType.NONE,
                    -1L
                )
            }

        return OverlayEntity(
            id,
            svgData,
            viewSpec,
            introTransitionSpec,
            outroTransitionSpec,
            overlayRelatedData.variablePlaceHolders
        )
    }

    fun toHideOverlayActionEntity(): HideOverlayActionEntity? {
        if (overlayRelatedData == null) {
            return null
        }
        return HideOverlayActionEntity(
            id,
            overlayRelatedData.id,
            overlayRelatedData.outroAnimationType,
            overlayRelatedData.outroAnimationDuration
        )
    }
    /**endregion */

    /**region Timer related*/
    fun toCreateTimerEntity(): CreateTimerEntity? {
        if (timerRelatedData == null) {
            return null
        }

        return CreateTimerEntity(
            timerRelatedData.name,
            offset,
            timerRelatedData.format,
            timerRelatedData.direction,
            timerRelatedData.startValue,
            timerRelatedData.step,
            timerRelatedData.capValue
        )
    }

    fun toStartTimerEntity(): StartTimerEntity? {
        if (timerRelatedData == null) {
            return null
        }

        return StartTimerEntity(
            timerRelatedData.name,
            offset
        )
    }

    fun toPauseTimerEntity(): PauseTimerEntity? {
        if (timerRelatedData == null) {
            return null
        }

        return PauseTimerEntity(timerRelatedData.name, offset)
    }

    fun toAdjustTimerEntity(): AdjustTimerEntity? {
        if (timerRelatedData == null) {
            return null
        }

        return AdjustTimerEntity(
            timerRelatedData.name,
            offset,
            timerRelatedData.value
        )
    }

    fun toSkipTimerEntity(): SkipTimerEntity? {
        if (timerRelatedData == null) {
            return null
        }
        return SkipTimerEntity(
            timerRelatedData.name,
            offset,
            timerRelatedData.value
        )
    }
    /**endregion */

    /**region Variable related*/
    fun toSetVariable(): SetVariableEntity? {
        if (rawData == null) {
            return null
        }

        if (type != SET_VARIABLE) {
            return null
        }

        var variableName = INVALID_STRING_VALUE
        var variableType = UNSPECIFIED
        var variableValue: Any = INVALID_LONG_VALUE
        var variableDoublePrecision: Int? = null

        rawData.let { data ->
            data.keys.forEach { key ->
                val any = data[key]
                when (key) {
                    "name" -> {
                        any?.let { variableName = it as String }
                    }
                    "type" -> {
                        any?.let { variableType = VariableType.fromValueOrNone(it as String) }
                    }
                    "double_precision" -> {
                        any?.let {
                            when (it) {
                                is Double -> {
                                    variableDoublePrecision = it.toInt()
                                }
                                is Int -> {
                                    variableDoublePrecision = it
                                }
                                else -> {
                                    // should not happen
                                }
                            }
                        }
                    }
                }
            }
        }

        when (rawData["value"]) {
            is Double -> {
                when (variableType) {
                    DOUBLE -> {
                        variableValue =
                            rawData["value"] as Double
                    }
                    LONG -> {
                        variableValue =
                            (rawData["value"] as Double).toLong()
                    }
                    STRING,
                    UNSPECIFIED -> {
                        // should not happen
                    }
                }
            }
            is Long -> {
                when (variableType) {
                    DOUBLE -> {
                        variableValue =
                            (rawData["value"] as Long).toDouble()
                    }
                    LONG -> {
                        variableValue =
                            rawData["value"] as Long
                    }
                    STRING,
                    UNSPECIFIED -> {
                        // should not happen
                    }
                }
            }
            else -> {
                // should not happen
            }
        }


        val kVariable = when (variableType) {
            UNSPECIFIED ->
                Variable.InvalidVariable(variableName)
            DOUBLE -> {
                Variable.DoubleVariable(
                    variableName,
                    variableValue as Double,
                    variableDoublePrecision
                )
            }
            LONG -> {
                Variable.LongVariable(variableName, variableValue as Long)
            }
            STRING -> {
                Variable.StringVariable(variableName, variableValue as String)
            }
        }
        return SetVariableEntity(id, offset, kVariable)
    }

    fun toIncrementVariableEntity(): IncrementVariableEntity? {
        if (rawData == null) {
            return null
        }

        if (type != INCREMENT_VARIABLE) {
            return null
        }

        var variableName = INVALID_STRING_VALUE
        var variableAmount: Any = INVALID_FLOAT_VALUE


        rawData.let { data ->
            data.keys.forEach { key ->
                val any = data[key]
                when (key) {
                    "name" -> {
                        any?.let { variableName = it as String }
                    }
                    "amount" -> {
                        any?.let {
                            when (it) {
                                is Double -> {
                                    variableAmount = it as Double
                                }
                                is Long -> {
                                    variableAmount = it as Long
                                }
                                else -> {
                                    // should not happen
                                }
                            }
                        }
                    }
                }
            }
        }

        return IncrementVariableEntity(
            id,
            offset,
            variableName,
            variableAmount
        )
    }
    /**endregion */

    /**region Timeline marker related*/
    fun toTimelineMarkerEntity(): TimelineMarkerEntity? {
        if (rawData == null) {
            return null
        }

        if (type != SHOW_TIMELINE_MARKER) {
            return null
        }

        var label = INVALID_STRING_VALUE
        var color = INVALID_STRING_VALUE
        var seekOffset = 0L // initialValue

        rawData.let { data ->
            data.keys.forEach { key ->
                val any = data[key]
                when (key) {

                    "seek_offset" -> {
                        any?.let { seekOffset = (it as Double).toLong() }
                    }
                    "label" -> {
                        any?.let { label = it as String }
                    }

                    "color" -> {
                        any?.let { color = it as String }
                    }
                    else -> {
                    }
                }
            }
        }

        return TimelineMarkerEntity(id, offset, seekOffset, label, color)
    }
    /**endregion */
}