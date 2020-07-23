package tv.mycujoo.data.entity

import tv.mycujoo.domain.entity.ActionEntity
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.domain.entity.TimelineMarkerEntity
import tv.mycujoo.mls.widgets.*

data class ActionCollections(
    var actionEntityList: List<ActionEntity>,
    var setVariableEntityList: List<SetVariableEntity>,
    var incrementVariableEntityList: List<IncrementVariableEntity>,
    var timelineMarkerActionList: List<TimelineMarkerEntity>,
    var createTimerEntityList: List<CreateTimerEntity>,
    var startTimerEntityList: List<StartTimerEntity>,
    var pauseTimerEntityList: List<PauseTimerEntity>,
    var adjustTimerEntityList: List<AdjustTimerEntity>,
    var skipTimerEntityList: List<SkipTimerEntity>,
    var timerEntity: List<TimerEntity>
)