package tv.mycujoo.mcls.core

import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.mcls.api.PlayerViewContract
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.helper.*
import tv.mycujoo.mcls.manager.IVariableKeeper
import tv.mycujoo.mcls.manager.TimerEntity
import tv.mycujoo.mcls.manager.TimerVariable
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.utils.TimeUtils
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

class AnnotationFactory @Inject constructor(
    private val annotationListener: IAnnotationListener,
    private val variableKeeper: IVariableKeeper,
    private val player: IPlayer
) : IAnnotationFactory {

    /**region Fields*/
    private var sortedActions =
        CopyOnWriteArrayList<Action>() // actions, sorted by offset, then by priority
    private var adjustedActions =
        CopyOnWriteArrayList<Action>() // sortedActionList + adjusted offset time
    private var timeSystem = TimeSystem.RELATIVE

    private var onScreenOverlayIds =
        CopyOnWriteArrayList<String>() // on-screen overlay actions cid

    private var localActions =
        CopyOnWriteArrayList<Action>() // local Actions which will be merged with server defined actions

    private var isMCLSActions = false

    private var allActions =
        CopyOnWriteArrayList<Action>() // union of Sorted actions + Local actions

    /**endregion */

    override fun attachPlayerView(playerView: PlayerViewContract) {
        annotationListener.attachPlayer(playerView)
    }

    /**
     * Set Local Actions, used for Mapped GQL events
     * @param actions List of Mapped GQL events to List<Action>
     */
    override fun setActions(actions: List<Action>) {
        if (isMCLSActions) {
            return
        }

        val sortedTemp = actions
            .sortedWith(compareBy<Action> { it.offset }.thenByDescending { it.priority })

        val deleteActions = ArrayList<Action>()
        deleteActions.addAll(sortedTemp.filterIsInstance<Action.DeleteAction>())

        sortedActions.clear()
        sortedActions.addAll(sortedTemp.filter { actionObject ->
            deleteActions.none {
                actionObject.id == it.id
            }
        })
    }

    /**
     * Set Actions, used for MCLS Events
     * @param annotations List of MCLS Actions
     */
    override fun setMCLSActions(annotations: List<Action>) {
        if (annotations.isEmpty()) {
            return
        }

        isMCLSActions = true
        val sortedTemp = annotations
            .sortedWith(compareBy<Action> { it.offset }.thenByDescending { it.priority })

        val deleteActions = ArrayList<Action>()
        deleteActions.addAll(sortedTemp.filterIsInstance<Action.DeleteAction>())

        sortedActions.clear()
        sortedActions.addAll(sortedTemp.filter { actionObject ->
            deleteActions.none {
                actionObject.id == it.id
            }
        })
    }

    override fun build() {
        allActions.apply {
            clear()
            addAll(localActions)
            addAll(sortedActions)
        }

        val currentTimeInInDvrWindowDuration = TimeRangeHelper.isCurrentTimeInDvrWindowDuration(
            player.duration(),
//            buildPoint.player.dvrWindowSize()
            Long.MAX_VALUE // todo! This should be filled from Stream's dvr-window size value
        )

        if (currentTimeInInDvrWindowDuration) {
            timeSystem = TimeSystem.RELATIVE
            adjustedActions.clear()
            process(
                currentTimeInInDvrWindowDuration,
                allActions
            )

        } else {
            timeSystem = TimeSystem.ABSOLUTE
            adjustedActions.clear()

            allActions.forEach { action ->
                val newOffset = TimeUtils.calculateOffset(
                    player.dvrWindowStartTime(),
                    action.absoluteTime
                )
                adjustedActions.add(action.updateOffset(newOffset))
            }
            process(
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
        isInDvrWindow: Boolean,
        list: List<Action>
    ) {
        val showOverlayList = arrayListOf<Action.ShowOverlayAction>()
        val hideOverlayList = arrayListOf<Action.HideOverlayAction>()

        val timerVariables: HashMap<String, TimerVariable> = HashMap()
        val varVariables: HashMap<String, VariableEntity> = HashMap()

        val timelineMarkers = ArrayList<TimelineMarkerEntity>()

        list.forEach { action ->
            val isInGap = player.isWithinValidSegment(action.absoluteTime)?.not() ?: false
            when (action) {
                is Action.ShowOverlayAction -> {
                    if (isInDvrWindow.not() && isInGap) {
                        return@forEach
                    }
                    addShowOverlayActionIfEligible(action, showOverlayList)
                }
                is Action.HideOverlayAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        hideOverlayList.add(action)
                    }
                }
                is Action.ReshowOverlayAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        list.firstOrNull { it is Action.ShowOverlayAction && it.customId == action.customId }
                            ?.let { relatedShowAction ->
                                val updatedAction =
                                    relatedShowAction.updateOffset(action.offset) as Action.ShowOverlayAction
                                addShowOverlayActionIfEligible(updatedAction, showOverlayList)
                            }
                    }
                }
                is Action.CreateTimerAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        createTimer(action, timerVariables)
                    }
                }
                is Action.StartTimerAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        startTimer(action, timerVariables)
                    }
                }
                is Action.PauseTimerAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        pauseTimer(action, timerVariables)
                    }
                }
                is Action.AdjustTimerAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        adjustTimer(action, timerVariables)
                    }
                }
                is Action.SkipTimerAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        skipTimer(action, timerVariables)
                    }
                }
                is Action.CreateVariableAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        createVariable(action, varVariables)
                    }
                }
                is Action.IncrementVariableAction -> {
                    if (action.isTillNowOrInRange(player.currentPosition())) {
                        incrementVariable(action, varVariables)
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
            player.currentPosition(), showOverlayList, hideOverlayList
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
                                player.currentPosition() - showOverlayAction.introTransitionSpec!!.offset,
                                player.isPlaying()
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
    ) {
        timerVariables[action.name]?.start(
            TimerEntity.StartTimer(action.name, action.offset),
            player.currentPosition()
        )

    }

    private fun pauseTimer(
        action: Action.PauseTimerAction,
        timerVariables: HashMap<String, TimerVariable>
    ) {
        timerVariables[action.name]?.pause(
            TimerEntity.PauseTimer(action.name, action.offset),
            player.currentPosition()
        )

    }

    private fun adjustTimer(
        action: Action.AdjustTimerAction,
        timerVariables: HashMap<String, TimerVariable>,
    ) {
        timerVariables[action.name]?.adjust(
            TimerEntity.AdjustTimer(action.name, action.offset, action.value),
            player.currentPosition()
        )

    }

    private fun skipTimer(
        action: Action.SkipTimerAction,
        timerVariables: HashMap<String, TimerVariable>
    ) {
        timerVariables[action.name]?.skip(
            TimerEntity.SkipTimer(action.name, action.offset, action.value),
            player.currentPosition()
        )
    }

    private fun createVariable(
        action: Action.CreateVariableAction,
        varVariables: HashMap<String, VariableEntity>
    ) {
        if (player.currentPosition() + ONE_SECOND_IN_MS > action.offset) {
            variableKeeper.createVariablePublisher(action.variable.name)
            varVariables[action.variable.name] =
                VariableEntity(action.id, action.offset, action.variable.copy())
        }
    }

    private fun incrementVariable(
        action: Action.IncrementVariableAction,
        varVariables: HashMap<String, VariableEntity>
    ) {
        if (player.currentPosition() + ONE_SECOND_IN_MS > action.offset) {
            varVariables[action.name]?.variable?.increment(action.amount)
        }
    }

    /**endregion */

    override fun clearOverlays() {
        isMCLSActions = false
        localActions.clear()
        annotationListener.clearScreen()
    }
}
