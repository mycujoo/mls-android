package tv.mycujoo.domain.entity

import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData
import tv.mycujoo.domain.entity.models.ParsedTimerRelatedData
import tv.mycujoo.domain.mapper.ActionMapper
import tv.mycujoo.domain.mapper.ActionMapper.Companion.INVALID_FLOAT_VALUE
import tv.mycujoo.domain.mapper.ActionMapper.Companion.INVALID_LONG_VALUE
import tv.mycujoo.domain.mapper.ActionMapper.Companion.INVALID_STRING_VALUE
import tv.mycujoo.mls.entity.*

data class ActionObject(
    val id: String,
    val type: ActionType,
    val offset: Long,
    val overlayRelatedData: ParsedOverlayRelatedData?,
    val timerRelatedData: ParsedTimerRelatedData?,
    val rawData: Map<String, Any>?
) {

    var priority: Int = 0

    init {
        priority = when (type) {
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
            null,
            null
        )
        val viewSpec = ViewSpec(overlayRelatedData.positionGuide, overlayRelatedData.sizePair)
        val introTransitionSpec = TransitionSpec(
            offset,
            overlayRelatedData.introAnimationType,
            overlayRelatedData.introAnimationDuration
        )

        val outroTransitionSpec: TransitionSpec =
            if (overlayRelatedData.duration > 0L) {
                TransitionSpec(
                    offset + overlayRelatedData.duration,
                    if (overlayRelatedData.outroAnimationType == AnimationType.UNSPECIFIED) {
                        AnimationType.NONE
                    } else {
                        overlayRelatedData.outroAnimationType
                    },
                    overlayRelatedData.outroAnimationDuration
                )
            } else {
                TransitionSpec(
                    -1L,
                    AnimationType.UNSPECIFIED,
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
        var variableType = VariableType.UNSPECIFIED
        var variableValue: Any = INVALID_LONG_VALUE
        var variableDoublePrecision = ActionMapper.INVALID_INT_VALUE

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
                        any?.let { variableDoublePrecision = (it as Double).toInt() }
                    }
                }
            }
        }

        when (variableType) {
            VariableType.DOUBLE -> {
                variableValue =
                    rawData["value"] as? Double ?: ActionMapper.INVALID_LONG_VALUE
            }
            VariableType.LONG -> {
                variableValue =
                    (rawData["value"] as? Double)?.toLong()
                        ?: ActionMapper.INVALID_LONG_VALUE
            }
            VariableType.STRING -> {
                variableValue =
                    rawData["value"] as? String ?: ActionMapper.INVALID_LONG_VALUE
            }
            VariableType.UNSPECIFIED -> {
                // should not happen
            }
        }

        val variable = Variable(variableName, variableType, variableValue)
        return SetVariableEntity(id, offset, variable)
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
                            variableAmount = it as Double
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
}