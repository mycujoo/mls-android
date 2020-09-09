package tv.mycujoo.domain.mapper

import tv.mycujoo.domain.entity.ActionSourceData
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.domain.entity.models.ActionType

class TimelineMarkerMapper {
    companion object {
        fun mapToTimelineMarker(actionSourceData: ActionSourceData?): TimelineMarkerEntity? {
            if (actionSourceData?.id == null || actionSourceData.offset == null || actionSourceData.type == null) {
                return null
            }

            val actionType = ActionType.fromValueOrUnknown(actionSourceData.type)
            if (actionType != ActionType.SHOW_TIMELINE_MARKER) {
                return null
            }

            var label = ActionMapper.INVALID_STRING_VALUE
            var color = ActionMapper.INVALID_STRING_VALUE
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
    }
}