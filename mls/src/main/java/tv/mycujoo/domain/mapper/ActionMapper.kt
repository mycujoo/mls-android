package tv.mycujoo.domain.mapper

import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.VariableType.*
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.model.MutablePair
import kotlin.random.Random

class ActionMapper {
    companion object {

        private const val INVALID_STRING_VALUE = "-1"
        private const val INVALID_LONG_VALUE = -1L
        private const val INVALID_INT_VALUE = -1
        private const val INVALID_FLOAT_VALUE = -1F

        fun mapToActionCollections(actionResponse: ActionResponse): ActionCollections {

            val actionEntityList = actionResponse.data.map { actionSourceData ->
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

            return ActionCollections(
                actionEntityList,
                setVariableList,
                incrementVariableList,
                timelineMarkerEntityList
            )

        }


        private fun mapToSetVariableEntity(actionSourceData: ActionSourceData): SetVariableEntity? {

            if (actionSourceData.id == null || actionSourceData.offset == null) {
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

        private fun mapToIncrementVariableEntity(actionSourceData: ActionSourceData): IncrementVariableEntity? {
            if (actionSourceData.id == null || actionSourceData.offset == null) {
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


        private fun mapToActionEntity(actionSourceData: ActionSourceData): ActionEntity {


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

            var variablePlaceHolders = emptyMap<String, String>()


            actionSourceData.data?.let { data ->
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
                            any?.let { variablePlaceHolders = it as Map<String, String> }
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


        private fun mapToTimelineMarkerEntity(actionSourceData: ActionSourceData): TimelineMarkerEntity? {
            if (actionSourceData.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.SHOW_TIMELINE_MARKER) {
                return null
            }

            var label = INVALID_STRING_VALUE
            var color = INVALID_STRING_VALUE

            actionSourceData.data?.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {

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



            return TimelineMarkerEntity(actionSourceData.id, actionSourceData.offset, label, color)
        }
    }
}