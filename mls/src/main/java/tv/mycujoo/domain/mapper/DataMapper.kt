package tv.mycujoo.domain.mapper

import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData
import tv.mycujoo.domain.entity.models.ParsedTimerRelatedData
import tv.mycujoo.mls.model.MutablePair
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat

class DataMapper {
    companion object {
        private const val INVALID_STRING_VALUE = "-1"
        private const val INVALID_LONG_VALUE = -1L
        private const val INVALID_INT_VALUE = -1
        private const val INVALID_FLOAT_VALUE = -1F

        fun parseOverlayRelatedData(rawDataMap: Map<String, Any>?): ParsedOverlayRelatedData? {
            if (rawDataMap == null) {
                return null
            }

            var newId = INVALID_STRING_VALUE
            var svgUrl = INVALID_STRING_VALUE
            var duration = INVALID_LONG_VALUE
            val positionGuide = PositionGuide()
            val sizePair = MutablePair(INVALID_FLOAT_VALUE, INVALID_FLOAT_VALUE)

            var introAnimationType = AnimationType.NONE
            var introAnimationDuration = INVALID_LONG_VALUE
            var outroAnimationType = AnimationType.UNSPECIFIED
            var outroAnimationDuration = INVALID_LONG_VALUE

            var variablePlaceHolders = emptyList<String>()

            rawDataMap.let { data ->
                data.keys.forEach { key ->
                    val any = data[key]
                    when (key) {
                        "custom_id" -> {
                            any?.let { newId = it as String }
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

//                        "label" -> {
//                            any?.let { label = it as String }
//                        }
//
//                        "color" -> {
//                            any?.let { color = it as String }
//                        }
                        "variable_positions" -> {
                            any?.let { variablePlaceHolders = it as List<String> }
                        }

                        else -> {
                        }
                    }
                }
                return ParsedOverlayRelatedData(
                    newId,
                    svgUrl,
                    duration,
                    positionGuide,
                    Pair(sizePair.first, sizePair.second),
                    introAnimationType,
                    introAnimationDuration,
                    outroAnimationType,
                    outroAnimationDuration,
                    variablePlaceHolders
                )
            }

            return null
        }

        fun parseTimerRelatedData(rawDataMap: Map<String, Any>?): ParsedTimerRelatedData? {
            if (rawDataMap == null) {
                return null
            }

            var name = INVALID_STRING_VALUE
            var format = ScreenTimerFormat.MINUTES_SECONDS
            var direction = ScreenTimerDirection.UP
            var startValue = INVALID_LONG_VALUE
            var step = INVALID_LONG_VALUE
            var capValue = INVALID_LONG_VALUE
            var value = INVALID_LONG_VALUE

            rawDataMap.let { data ->
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
                        "value" -> {
                            any?.let { value = (it as Double).toLong() }
                        }
                        else -> {
                        }
                    }
                }

                return ParsedTimerRelatedData(name, format, direction, startValue, step, capValue, value)
            }
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
    }
}