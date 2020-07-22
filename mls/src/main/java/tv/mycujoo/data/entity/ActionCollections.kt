package tv.mycujoo.data.entity

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity

data class ActionCollections(
    var actionEntityList: List<ActionEntity>,
    var setVariableEntityList: List<SetVariableEntity>,
    var incrementVariableEntityList: List<IncrementVariableEntity>,
    var timelineMarkerActionList: List<TimelineMarkerEntity>
)