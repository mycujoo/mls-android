package tv.mycujoo.domain.mapper

import tv.mycujoo.domain.entity.ActionSourceData
import tv.mycujoo.domain.entity.AnimationType
import tv.mycujoo.domain.entity.NEWActionEntity
import tv.mycujoo.domain.entity.PositionGuide
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.mls.model.MutablePair

class ActionMapper {
    companion object {

        const val INVALID_STRING_VALUE = "-1"
        const val INVALID_LONG_VALUE = -1L
        const val INVALID_FLOAT_VALUE = -1F

        fun mapToEntity(offset: Long, actionSourceData: ActionSourceData): NEWActionEntity {


            var customId = INVALID_STRING_VALUE
            var svgUrl = INVALID_STRING_VALUE
            var duration = INVALID_LONG_VALUE
            val positionGuide = PositionGuide()
            val sizePair = MutablePair(INVALID_FLOAT_VALUE, INVALID_FLOAT_VALUE)

            var introAnimationType = AnimationType.NONE
            var introAnimationDuration = INVALID_FLOAT_VALUE
            var outroAnimationType = AnimationType.NONE
            var outroAnimationDuration = INVALID_FLOAT_VALUE


            var label = INVALID_STRING_VALUE


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
                            any?.let { introAnimationDuration = (it as Double).toFloat() }

                        }
                        "animateout_type" -> {
                            any?.let {
                                outroAnimationType = AnimationType.fromValueOrNone(it as String)
                            }
                        }
                        "animateout_duration" -> {
                            any?.let { outroAnimationDuration = (it as Double).toFloat() }
                        }

                        "label" -> {
                            any?.let { label = it as String }
                        }

                        else -> {
                        }
                    }
                }
            }


            return NEWActionEntity(
                actionSourceData.id!!,
                offset,
                ActionType.fromValueOrUnknown(actionSourceData.type!!),
                customId,
                svgUrl,
                positionGuide,
                sizePair,
                duration,
                introAnimationType,
                introAnimationDuration,
                outroAnimationType,
                outroAnimationDuration,
                label
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
                    "trailing" -> {
                        positionGuide.trailing = (map[key]!!.toFloat())
                    }
                    "bottom" -> {
                        positionGuide.bottom = (map[key]!!.toFloat())
                    }
                    "leading" -> {
                        positionGuide.leading = (map[key]!!.toFloat())
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