package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.helper.*
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.TimerEntity
import tv.mycujoo.mls.manager.TimerVariable
import tv.mycujoo.mls.utils.TimeUtils
import java.util.concurrent.CopyOnWriteArrayList

class AnnotationFactory(
    private val annotationListener: IAnnotationListener,
    private val variableKeeper: IVariableKeeper
) :
    IAnnotationFactory {

    private var sortedActions =
        CopyOnWriteArrayList<Action>()// actions, sorted by offset, then by priority
    private var adjustedActions =
        CopyOnWriteArrayList<Action>()// sortedActionList + adjusted offset time
    private var timeSystem = TimeSystem.RELATIVE

    private var onScreenOverlayIds =
        CopyOnWriteArrayList<String>()// on-screen overlay actions cid


    /**region Over-ridden functions*/
    override fun setActions(actions: List<Action>) {
        val sortedTemp =
            actions
                .sortedWith(compareBy<Action> { it.offset }.thenByDescending { it.priority })

        val deleteActions = ArrayList<Action>()
        deleteActions.addAll(sortedTemp.filterIsInstance<Action.DeleteAction>())

        sortedActions.clear()
        sortedActions.addAll(sortedTemp.filter { actionObject -> deleteActions.none { actionObject.id == it.id } })
    }

    override fun build(buildPoint: BuildPoint) {
        val currentTimeInInDvrWindowDuration = TimeRangeHelper.isCurrentTimeInDvrWindowDuration(
            buildPoint.player.duration(),
//            buildPoint.player.dvrWindowSize()
            Long.MAX_VALUE // todo! This should be filled from Stream's dvr-window size value
        )

        if (currentTimeInInDvrWindowDuration) {
            timeSystem = TimeSystem.RELATIVE
            adjustedActions.clear()
            process(
                buildPoint,
                currentTimeInInDvrWindowDuration,
                sortedActions
            )

        } else {
            timeSystem = TimeSystem.ABSOLUTE
            adjustedActions.clear()

            sortedActions.forEach { action ->
                val newOffset = TimeUtils.calculateOffset(
                    buildPoint.player.dvrWindowStartTime(),
                    action.absoluteTime
                )
                adjustedActions.add(action.updateOffset(newOffset))
            }
            process(
                buildPoint,
                currentTimeInInDvrWindowDuration,
                adjustedActions
            )

        }

    }

    override fun getCurrentActions(): List<Action> {
        return if (adjustedActions.isNotEmpty()) {
            adjustedActions
        } else {
            sortedActions
        }
    }

    /**endregion */

    /**region Processing actions*/
    private fun process(
        buildPoint: BuildPoint,
        isInDvrWindow: Boolean,
        list: List<Action>
    ) {
        val showOverlayList = arrayListOf<Action.ShowOverlayAction>()
        val hideOverlayList = arrayListOf<Action.HideOverlayAction>()

        val timerVariables: HashMap<String, TimerVariable> = HashMap()
        val varVariables: HashMap<String, VariableEntity> = HashMap()

        val timelineMarkers = ArrayList<TimelineMarkerEntity>()

        list.forEach { action ->
            val isInGap =
                buildPoint.player.isWithinValidSegment(action.absoluteTime)?.not() ?: false
            when (action) {
                is Action.ShowOverlayAction -> {
                    if (isInDvrWindow.not() && isInGap) {
                        return@forEach
                    }
                    addShowOverlayActionIfEligible(action, showOverlayList)
                }
                is Action.HideOverlayAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        hideOverlayList.add(action)
                    }
                }
                is Action.ReshowOverlayAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        list.firstOrNull { it is Action.ShowOverlayAction && it.customId == action.customId }
                            ?.let { relatedShowAction ->
                                val updatedAction =
                                    relatedShowAction.updateOffset(action.offset) as Action.ShowOverlayAction
                                addShowOverlayActionIfEligible(updatedAction, showOverlayList)
                            }
                    }
                }
                is Action.CreateTimerAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        createTimer(action, timerVariables)
                    }
                }
                is Action.StartTimerAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        startTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.PauseTimerAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        pauseTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.AdjustTimerAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        adjustTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.SkipTimerAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        skipTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.CreateVariableAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        createVariable(action, buildPoint, varVariables)
                    }
                }
                is Action.IncrementVariableAction -> {
                    if (action.isTillNowOrInRange(buildPoint.currentRelativePosition)) {
                        incrementVariable(action, buildPoint, varVariables)
                    }
                }
                is Action.MarkTimelineAction -> {
                    if (shouldMarkTimeLine(action)) {
                        timelineMarkers.add(
                            TimelineMarkerEntity(
                                action.id,
                                action.offset,
                                action.seekOffset,
                                action.label,
                                action.color
                            )
                        )
                    }
                }
                is Action.DeleteAction,
                is Action.InvalidAction -> {
                    // do nothing
                }
            }
        }

        val act = ActionActor().act(
            buildPoint.currentRelativePosition, showOverlayList, hideOverlayList
        )
        act.forEach { pair ->
            when (pair.second) {
                is Action.ShowOverlayAction -> {
                    val showOverlayAction = pair.second as Action.ShowOverlayAction
                    when (pair.first) {
                        ActionActor.ActionAct.INTRO -> {
                            if (onScreenOverlayIds.none { it == showOverlayAction.customId }) {
                                onScreenOverlayIds.add(showOverlayAction.customId)
                                annotationListener.addOverlay(showOverlayAction)
                            }
                        }
                        ActionActor.ActionAct.MIDWAY -> {
                            if (onScreenOverlayIds.none { it == showOverlayAction.customId }) {
                                onScreenOverlayIds.add(showOverlayAction.customId)
                            }
                            annotationListener.addOrUpdateLingeringMidwayOverlay(showOverlayAction)
                        }
                        ActionActor.ActionAct.OUTRO -> {
                            if (onScreenOverlayIds.any { it == showOverlayAction.customId }) {
                                onScreenOverlayIds.remove(showOverlayAction.customId)
                                annotationListener.removeOverlay(
                                    showOverlayAction.customId,
                                    showOverlayAction.outroTransitionSpec
                                )
                            }
                        }
                        ActionActor.ActionAct.LINGERING_INTRO -> {
                            if (onScreenOverlayIds.none { it == showOverlayAction.customId }) {
                                onScreenOverlayIds.add(showOverlayAction.customId)
                            }
                            annotationListener.addOrUpdateLingeringIntroOverlay(
                                showOverlayAction,
                                buildPoint.currentRelativePosition - showOverlayAction.introTransitionSpec!!.offset,
                                buildPoint.isPlaying
                            )

                        }
                        ActionActor.ActionAct.REMOVE -> {
                            if (onScreenOverlayIds.any { it == showOverlayAction.customId }) {
                                onScreenOverlayIds.remove(showOverlayAction.customId)
                                annotationListener.removeOverlay(showOverlayAction.customId, null)
                            }
                        }
                        ActionActor.ActionAct.DO_NOTHING -> {
                            // do nothing
                        }

                    }
                }
                is Action.HideOverlayAction -> {
                    val hideOverlayAction = pair.second as Action.HideOverlayAction
                    when (pair.first) {
                        ActionActor.ActionAct.OUTRO -> {
                            if (onScreenOverlayIds.any { it == hideOverlayAction.customId }) {
                                onScreenOverlayIds.remove(hideOverlayAction.customId)
                                annotationListener.removeOverlay(
                                    hideOverlayAction.customId,
                                    hideOverlayAction.outroTransitionSpec
                                )
                            }
                        }
                        ActionActor.ActionAct.REMOVE -> {
                            if (onScreenOverlayIds.any { it == hideOverlayAction.customId }) {
                                onScreenOverlayIds.remove(hideOverlayAction.customId)
                                annotationListener.removeOverlay(
                                    hideOverlayAction.customId, null
                                )
                            }
                        }
                        else -> {
                            // should not happen
                        }
                    }
                }
                else -> {
                    // should not happen
                }
            }


        }


        variableKeeper.notifyTimers(timerVariables)
        variableKeeper.notifyVariables(varVariables)

        annotationListener.setTimelineMarkers(timelineMarkers)
    }

    private fun addShowOverlayActionIfEligible(
        action: Action.ShowOverlayAction,
        showOverlayList: ArrayList<Action.ShowOverlayAction>
    ) {
        if (action.isEligible()) {
            showOverlayList.add(action)
        }
    }


    private fun shouldMarkTimeLine(
        action: Action.MarkTimelineAction
    ): Boolean {
        if (action.offset < 0L) {
            return false
        }
        return true
    }

    private fun createTimer(
        action: Action.CreateTimerAction,
        timerVariables: HashMap<String, TimerVariable>
    ) {
        variableKeeper.createTimerPublisher(action.name)

        timerVariables[action.name] =
            TimerVariable(
                action.name,
                action.format,
                action.direction,
                action.startValue,
                action.capValue
            )

    }

    private fun startTimer(
        action: Action.StartTimerAction,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        timerVariables[action.name]?.start(
            TimerEntity.StartTimer(action.name, action.offset),
            buildPoint.currentRelativePosition
        )

    }

    private fun pauseTimer(
        action: Action.PauseTimerAction,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        timerVariables[action.name]?.pause(
            TimerEntity.PauseTimer(action.name, action.offset),
            buildPoint.currentRelativePosition
        )

    }

    private fun adjustTimer(
        action: Action.AdjustTimerAction,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        timerVariables[action.name]?.adjust(
            TimerEntity.AdjustTimer(action.name, action.offset, action.value),
            buildPoint.currentRelativePosition
        )

    }

    private fun skipTimer(
        action: Action.SkipTimerAction,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        timerVariables[action.name]?.skip(
            TimerEntity.SkipTimer(action.name, action.offset, action.value),
            buildPoint.currentRelativePosition
        )
    }

    private fun createVariable(
        action: Action.CreateVariableAction,
        buildPoint: BuildPoint,
        varVariables: HashMap<String, VariableEntity>
    ) {
        if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS > action.offset) {
            variableKeeper.createVariablePublisher(action.variable.name)
            varVariables[action.variable.name] =
                VariableEntity(action.id, action.offset, action.variable.copy())
        }
    }

    private fun incrementVariable(
        action: Action.IncrementVariableAction,
        buildPoint: BuildPoint,
        varVariables: HashMap<String, VariableEntity>
    ) {
        if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS > action.offset) {
            varVariables[action.name]?.let { variableEntity ->
                variableEntity.variable.increment(action.amount)
            }
        }
    }


    /**endregion */
}