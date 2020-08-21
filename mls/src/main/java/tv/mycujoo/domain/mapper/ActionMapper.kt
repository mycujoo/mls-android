package tv.mycujoo.domain.mapper

import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.VariableType.*
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.model.MutablePair
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.widgets.*
import kotlin.random.Random

class ActionMapper {
    companion object {

        private const val INVALID_STRING_VALUE = "-1"
        private const val INVALID_LONG_VALUE = -1L
        private const val INVALID_INT_VALUE = -1
        private const val INVALID_FLOAT_VALUE = -1F

        fun mapToActionCollections(actionResponse: ActionResponse): ActionCollections {

            val actionEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToActionEntity(actionSourceData)
            }

            val setVariableList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToSetVariableEntity(actionSourceData)
            }

            val incrementVariableList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToIncrementVariableEntity(actionSourceData)
            }

            val timelineMarkerEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToTimelineMarkerEntity(actionSourceData)
            }

            val createScreenTimerEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToCreateScreenTimerEntity(actionSourceData)
            }

            val startScreenTimerEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToStartScreenTimerEntity(actionSourceData)
            }

            val pauseTimerEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToPauseTimerEntity(actionSourceData)
            }

            val adjustTimerEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToAdjustTimerEntity(actionSourceData)
            }

            val skipTimerEntityList = actionResponse.data.mapNotNull { actionSourceData ->
                mapToSkipTimerEntity(actionSourceData)
            }

            val timerEntityList = ArrayList<TimerCollection>()
            createScreenTimerEntityList.forEach { createTimerEntity ->

                val createCommands = ArrayList<CreateTimerEntity>()
                val startCommands = ArrayList<StartTimerEntity>()
                val pauseCommands = ArrayList<PauseTimerEntity>()
                val adjustCommands = ArrayList<AdjustTimerEntity>()
                val skipCommands = ArrayList<SkipTimerEntity>()

                startScreenTimerEntityList.filter { it.name == createTimerEntity.name }.forEach {
                    startCommands.add(it)
                }
                pauseTimerEntityList.filter { it.name == createTimerEntity.name }.forEach {
                    pauseCommands.add(it)
                }
                adjustTimerEntityList.filter { it.name == createTimerEntity.name }.forEach {
                    adjustCommands.add(it)
                }
                skipTimerEntityList.filter { it.name == createTimerEntity.name }.forEach {
                    skipCommands.add(it)
                }


                val timerEntity = TimerCollection(
                    createTimerEntity.name,
                    createTimerEntity,
                    startCommands,
                    pauseCommands,
                    adjustCommands,
                    skipCommands
                )
                timerEntityList.add(timerEntity)
            }


            return ActionCollections(
                actionEntityList,
                setVariableList,
                incrementVariableList,
                timelineMarkerEntityList,
                createScreenTimerEntityList,
                startScreenTimerEntityList,
                pauseTimerEntityList,
                adjustTimerEntityList,
                skipTimerEntityList,
                timerEntityList
            )

        }


        private fun mapToSetVariableEntity(actionSourceData: ActionSourceData?): SetVariableEntity? {

            if (actionSourceData?.id == null || actionSourceData.offset == null) {
                return null
            }

            if (ActionType.fromValueOrUnknown(actionSourceData.type!!) != ActionType.SET_VARIABLE) {
                return null
            }

            var variableName = INVALID_STRING_VALUE
            var variableType = UNSPECIFIED
            var variableValue: Any = INVALID_LONG_VALUE
            var variableDoublePrecision = INVALID_INT_VALUE

            actionSourceData.data?.let { data ->
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
                DOUBLE -> {
                    variableValue =
                        actionSourceData.data?.get("value") as? Double ?: INVALID_LONG_VALUE
                }
                LONG -> {
                    variableValue =
                        (actionSourceData.data?.get("value") as? Double)?.toLong()
                            ?: INVALID_LONG_VALUE
                }
                STRING -> {
                    variableValue =
                        actionSourceData.data?.get("value") as? String ?: INVALID_LONG_VALUE
                }
                UNSPECIFIED -> {
                    // should not happen
                }
            }

            val variable = Variable(variableName, variableType, variableValue)

            return SetVariableEntity(actionSourceData.id, actionSourceData.offset, variable)
        }

        private fun mapToIncrementVariableEntity(actionSourceData: ActionSourceData?): IncrementVariableEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null) {
                return null
            }

            if (ActionType.fromValueOrUnknown(actionSourceData.type!!) != ActionType.INCREMENT_VARIABLE) {
                return null
            }

            var variableName = INVALID_STRING_VALUE
            var variableAmount: Any = INVALID_FLOAT_VALUE


            actionSourceData.data?.let { data ->
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
                actionSourceData.id,
                actionSourceData.offset,
                variableName,
                variableAmount
            )
        }


        private fun mapToActionEntity(actionSourceData: ActionSourceData?): ActionEntity? {
            if (actionSourceData == null || actionSourceData.data.isNullOrEmpty()) {
                return null
            }

            var customId = INVALID_STRING_VALUE
            var svgUrl = INVALID_STRING_VALUE
            var duration = INVALID_LONG_VALUE
            val positionGuide = PositionGuide()
            val sizePair = MutablePair(INVALID_FLOAT_VALUE, INVALID_FLOAT_VALUE)

            var introAnimationType = AnimationType.NONE
            var introAnimationDuration = INVALID_LONG_VALUE
            var outroAnimationType = AnimationType.UNSPECIFIED
            var outroAnimationDuration = INVALID_LONG_VALUE


            var label = INVALID_STRING_VALUE
            var color = INVALID_STRING_VALUE

            var variablePlaceHolders = emptyList<String>()


            actionSourceData.data.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {
                        "custom_id" -> {
                            any?.let { customId = it as String }
                        }
                        "svg_url" -> {
                            any?.let { svgUrl = it as String }
                        }
                        "position" -> {
                            any?.let { retrievePositionGuide(positionGuide, it) }
                        }
                        "size" -> {
                            any?.let { retrieveSize(sizePair, it) }
                        }
                        "duration" -> {
                            any?.let { duration = (it as Double).toLong() }
                        }
                        "animatein_type" -> {
                            any?.let {
                                introAnimationType = AnimationType.fromValueOrNone(it as String)
                            }
                        }
                        "animatein_duration" -> {
                            any?.let { introAnimationDuration = (it as Double).toLong() }

                        }
                        "animateout_type" -> {
                            any?.let {
                                outroAnimationType = AnimationType.fromValueOrNone(it as String)
                            }
                        }
                        "animateout_duration" -> {
                            any?.let { outroAnimationDuration = (it as Double).toLong() }
                        }

                        "label" -> {
                            any?.let { label = it as String }
                        }

                        "color" -> {
                            any?.let { color = it as String }
                        }
                        "variable_positions" -> {
                            any?.let { variablePlaceHolders = it as List<String> }
                        }

                        else -> {
                        }
                    }
                }
            }

            if (customId == INVALID_STRING_VALUE) {
                customId = Random.nextLong().toString()
            }

            return ActionEntity(
                actionSourceData.id!!,
                actionSourceData.offset ?: INVALID_LONG_VALUE,
                ActionType.fromValueOrUnknown(actionSourceData.type!!),
                customId,
                svgUrl,
                null,
                positionGuide,
                Pair(sizePair.first, sizePair.second),
                duration,
                introAnimationType,
                introAnimationDuration,
                outroAnimationType,
                outroAnimationDuration,
                variablePlaceHolders
            )
        }

        private fun retrieveSize(
            sizePair: MutablePair<Float, Float>,
            data: Any
        ) {
            val map = data as Map<String, Double>
            map.keys.forEach { key ->
                when (key) {
                    "width" -> {
                        sizePair.first = (map[key]!!.toFloat())
                    }
                    "height" -> {
                        sizePair.second = (map[key]!!.toFloat())
                    }

                    else -> {
                    }
                }
            }
        }

        private fun retrievePositionGuide(positionGuide: PositionGuide, data: Any) {
            val map = data as Map<String, Double>
            map.keys.forEach { key ->
                when (key) {
                    "top" -> {
                        positionGuide.top = (map[key]!!.toFloat())
                    }
                    "right" -> {
                        positionGuide.right = (map[key]!!.toFloat())
                    }
                    "bottom" -> {
                        positionGuide.bottom = (map[key]!!.toFloat())
                    }
                    "left" -> {
                        positionGuide.left = (map[key]!!.toFloat())
                    }
                    "HCenter" -> {
                        positionGuide.hCenter = (map[key]!!.toFloat())
                    }
                    "VCenter" -> {
                        positionGuide.vCenter = (map[key]!!.toFloat())
                    }

                    else -> {
                    }
                }
            }
        }


        private fun mapToTimelineMarkerEntity(actionSourceData: ActionSourceData?): TimelineMarkerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.SHOW_TIMELINE_MARKER) {
                return null
            }

            var label = INVALID_STRING_VALUE
            var color = INVALID_STRING_VALUE
            var seekOffset = 0L // initialValue

            actionSourceData.data?.let { data ->
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


            return TimelineMarkerEntity(actionSourceData.id, seekOffset, actionSourceData.offset, label, color)
        }

        private fun mapToCreateScreenTimerEntity(actionSourceData: ActionSourceData?): CreateTimerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.CREATE_TIMER) {
                return null
            }

            var name = INVALID_STRING_VALUE
            var format = ScreenTimerFormat.MINUTES_SECONDS
            var direction = ScreenTimerDirection.UP
            var startValue = INVALID_LONG_VALUE
            var step = INVALID_LONG_VALUE
            var capValue = INVALID_LONG_VALUE

            actionSourceData.data?.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {

                        "name" -> {
                            any?.let { name = it as String }
                        }
                        "format" -> {
                            any?.let { format = ScreenTimerFormat.fromValueOrUnknown(it as String) }
                        }
                        "direction" -> {
                            any?.let { direction = ScreenTimerDirection.fromValue(it as String) }
                        }
                        "startValue" -> {
                            any?.let { startValue = (it as Double).toLong() }
                        }
                        "step" -> {
                            any?.let { step = (it as Double).toLong() }
                        }
                        "cap_value" -> {
                            any?.let { capValue = (it as Double).toLong() }
                        }
                        else -> {
                        }
                    }
                }
            }

            return CreateTimerEntity(
                name,
                actionSourceData.offset,
                format,
                direction,
                startValue,
                step,
                capValue
            )
        }

        private fun mapToStartScreenTimerEntity(actionSourceData: ActionSourceData?): StartTimerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.START_TIMER) {
                return null
            }

            var name = INVALID_STRING_VALUE

            actionSourceData.data?.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {

                        "name" -> {
                            any?.let { name = it as String }
                        }
                        else -> {
                        }
                    }
                }
            }

            return StartTimerEntity(
                name,
                actionSourceData.offset
            )
        }

        private fun mapToPauseTimerEntity(actionSourceData: ActionSourceData?): PauseTimerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.PAUSE_TIMER) {
                return null
            }

            var name = INVALID_STRING_VALUE

            actionSourceData.data?.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {

                        "name" -> {
                            any?.let { name = it as String }
                        }
                        else -> {
                        }
                    }
                }
            }

            return PauseTimerEntity(
                name,
                actionSourceData.offset
            )
        }

        private fun mapToAdjustTimerEntity(actionSourceData: ActionSourceData?): AdjustTimerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.ADJUST_TIMER) {
                return null
            }

            var name = INVALID_STRING_VALUE
            var value = INVALID_LONG_VALUE

            actionSourceData.data?.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {

                        "name" -> {
                            any?.let { name = it as String }
                        }
                        "value" -> {
                            any?.let { value = (it as Double).toLong() }
                        }
                        else -> {
                        }
                    }
                }
            }

            return AdjustTimerEntity(
                name,
                actionSourceData.offset,
                value
            )
        }

        private fun mapToSkipTimerEntity(actionSourceData: ActionSourceData?): SkipTimerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.SKIP_TIMER) {
                return null
            }

            var name = INVALID_STRING_VALUE
            var value = INVALID_LONG_VALUE

            actionSourceData.data?.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {

                        "name" -> {
                            any?.let { name = it as String }
                        }
                        "value" -> {
                            any?.let { value = (it as Double).toLong() }
                        }
                        else -> {
                        }
                    }
                }
            }

            return SkipTimerEntity(
                name,
                actionSourceData.offset,
                value
            )
        }

    }
}