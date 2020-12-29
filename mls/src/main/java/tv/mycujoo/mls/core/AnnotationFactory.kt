package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.*
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.helper.*
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.TimerEntity
import tv.mycujoo.mls.manager.TimerVariable
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.utils.TimeUtils
import java.util.concurrent.CopyOnWriteArrayList

class AnnotationFactory(
    private val annotationListener: IAnnotationListener,
    private val viewHandler: IViewHandler,
    private val variableKeeper: IVariableKeeper
) :
    IAnnotationFactory {

    private var sortedActions =
        CopyOnWriteArrayList<Action>()// actions, sorted by offset, then by priority
    private var adjustedActions =
        CopyOnWriteArrayList<Action>()// sortedActionList + adjusted offset time
    private var timeSystem = TimeSystem.RELATIVE

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
            return process(
                buildPoint,
                currentTimeInInDvrWindowDuration,
                sortedActions
            )

        } else {
            timeSystem = TimeSystem.ABSOLUTE
            adjustedActions.clear()

            sortedActions.forEach { action ->
                val newOffset = TimeUtils.convertRelativeTimeToAbsolute(
                    buildPoint.player.dvrWindowStartTime(),
                    action.absoluteTime
                )
                adjustedActions.add(action.updateOffset(newOffset))
            }
            return process(
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
                    val act =
                        ShowOverlayActionHelper.getOverlayActionCurrentAct(
                            buildPoint.currentRelativePosition,
                            action,
                            buildPoint.isInterrupted
                        )
                    showOverlay(action, act, buildPoint)
                }
                is Action.HideOverlayAction -> {
                    val act =
                        HideOverlayActionHelper.getOverlayActionCurrentAct(
                            buildPoint.currentRelativePosition,
                            action,
                            buildPoint.isInterrupted,
                            list
                        )
                    hideOverlay(action, act)
                }
                is Action.ReshowOverlayAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        list.firstOrNull { it is Action.ShowOverlayAction && it.customId == action.customId }
                            ?.let {
                                showOverlay(
                                    it as Action.ShowOverlayAction,
                                    OverlayAct.INTRO,
                                    buildPoint
                                )
                            }
                    }
                }
                is Action.CreateTimerAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        createTimer(action, timerVariables)
                    }
                }
                is Action.StartTimerAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        startTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.PauseTimerAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        pauseTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.AdjustTimerAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        adjustTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.SkipTimerAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        skipTimer(action, timerVariables, buildPoint)
                    }
                }
                is Action.CreateVariableAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        createVariable(action, buildPoint, varVariables)
                    }
                }
                is Action.IncrementVariableAction -> {
                    if (buildPoint.currentRelativePosition + ONE_SECOND_IN_MS >= action.offset) {
                        incrementVariable(action, buildPoint, varVariables)
                    }
                }
                is Action.MarkTimelineAction -> {
                    if (shouldMarkTimeLine(buildPoint, action)) {
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

        variableKeeper.notifyTimers(timerVariables)
        variableKeeper.notifyVariables(varVariables)

        annotationListener.setTimelineMarkers(timelineMarkers)
    }

    private fun shouldMarkTimeLine(
        buildPoint: BuildPoint,
        action: Action.MarkTimelineAction
    ): Boolean {
        if (action.offset < 0L) {
            return false
        }
        return true
    }

    private fun showOverlay(
        action: Action.ShowOverlayAction,
        act: OverlayAct,
        buildPoint: BuildPoint
    ) {
        when (act) {
            OverlayAct.INTRO -> {
                annotationListener.addOverlay(action)
            }
            OverlayAct.OUTRO,
            OverlayAct.REMOVE -> {
                annotationListener.removeOverlay(action.id, action.outroTransitionSpec)
            }
            OverlayAct.DO_NOTHING -> {
                // do nothing
            }
            OverlayAct.LINGERING_INTRO -> {
                annotationListener.addOrUpdateLingeringIntroOverlay(
                    action,
                    buildPoint.currentRelativePosition - action.introTransitionSpec!!.offset,
                    buildPoint.isPlaying
                )

            }
            OverlayAct.LINGERING_MIDWAY -> {
                annotationListener.addOrUpdateLingeringMidwayOverlay(
                    action
                )
            }
            OverlayAct.LINGERING_OUTRO -> {
                annotationListener.addOrUpdateLingeringOutroOverlay(
                    action,
                    buildPoint.currentRelativePosition - (action.offset + action.outroTransitionSpec!!.animationDuration),
                    buildPoint.isPlaying
                )
            }
            OverlayAct.LINGERING_REMOVE -> {
                annotationListener.removeLingeringOverlay(action.id, action.outroTransitionSpec)
            }

        }
    }

    private fun hideOverlay(
        hideOverlayAction: Action.HideOverlayAction,
        overlayActionCurrentAct: HideOverlayAct
    ) {
        when (overlayActionCurrentAct) {
            HideOverlayAct.DO_NOTHING -> {
                // do nothing
            }
            HideOverlayAct.OUTRO_IN_RANGE -> {
                annotationListener.removeOverlay(
                    hideOverlayAction.customId,
                    hideOverlayAction.outroTransitionSpec
                )
            }
            HideOverlayAct.OUTRO_LINGERING -> {
                annotationListener.removeLingeringOverlay(
                    hideOverlayAction.customId!!,
                    hideOverlayAction.outroTransitionSpec
                )
            }
            HideOverlayAct.OUTRO_LEFTOVER -> {
                annotationListener.removeOverlay(hideOverlayAction.customId!!, null)
            }
        }
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
                VariableEntity(action.id, action.offset, action.variable)
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