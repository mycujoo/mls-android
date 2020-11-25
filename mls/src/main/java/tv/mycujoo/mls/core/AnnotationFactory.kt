package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.helper.*
import tv.mycujoo.mls.manager.TimerVariable
import tv.mycujoo.mls.manager.contracts.IViewHandler
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class AnnotationFactory(
    private val annotationListener: IAnnotationListener,
    private val viewHandler: IViewHandler,
    private val lock: ReentrantLock,
    private val busyCondition: Condition
) : IAnnotationFactory {

    private val variableKeeper = viewHandler.getVariableKeeper()
    private val atomicInt = AtomicInteger()

    private lateinit var sortedActionList: List<ActionObject>

    override fun setAnnotations(actionObjectList: List<ActionObject>) {
        lock.lock()
        if (atomicInt.get() > 0) {
            busyCondition.await()
        }
        atomicInt.incrementAndGet()

        val sortedTemp =
            actionObjectList
                .sortedWith(compareBy<ActionObject> { it.offset }.thenByDescending { it.priority })

        val deleteActions = ArrayList<ActionObject>()
        loop@ for (actionObject in sortedTemp) {
            if (actionObject.type != DELETE_ACTION) {
                break@loop
            }
            deleteActions.add(actionObject)
        }

        sortedActionList =
            sortedTemp.filter { actionObject -> deleteActions.none { actionObject.id == it.id } }


        atomicInt.decrementAndGet()
        busyCondition.signal()
        lock.unlock()
    }

    override fun actionList(): List<ActionObject> {
        return sortedActionList
    }

    override fun build(buildPoint: BuildPoint) {
        if (this::sortedActionList.isInitialized.not()) {
            return
        }
        lock.lock()
        if (atomicInt.get() > 0) {
            busyCondition.await()
        }
        atomicInt.incrementAndGet()

        val timerVariables: HashMap<String, TimerVariable> = HashMap()
        val varVariables: HashMap<String, SetVariableEntity> = HashMap()

        val timelineMarkers = ArrayList<TimelineMarkerEntity>()

        var currentTimeInDvrWindowDuration = TimeRangeHelper.isCurrentTimeInDvrWindowDuration(
            buildPoint.currentRelativePosition,
            Long.MAX_VALUE // todo! This should be filled from Stream's dvr-window size value
        )

        currentTimeInDvrWindowDuration = true

        if (currentTimeInDvrWindowDuration) {
            sortedActionList.forEach {
                when (it.type) {
                    SHOW_OVERLAY -> {
                        val act =
                            ShowOverlayActionHelper.getOverlayActionCurrentAct(
                                TimeSystem.RELATIVE,
                                buildPoint.currentRelativePosition,
                                it,
                                buildPoint.isInterrupted
                            )
                        showOverlay(it, act, buildPoint)
                    }
                    HIDE_OVERLAY -> {
                        val act =
                            HideOverlayActionHelper.getOverlayActionCurrentAct(
                                TimeSystem.RELATIVE,
                                buildPoint.currentRelativePosition,
                                it
                            )
                        hideOverlay(it, act)
                    }
                    CREATE_TIMER -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            createTimer(it, timerVariables)
                        }
                    }
                    START_TIMER -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            startTimer(it, timerVariables, buildPoint)
                        }
                    }
                    PAUSE_TIMER -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            pauseTimer(it, timerVariables, buildPoint)
                        }
                    }
                    ADJUST_TIMER -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            adjustTimer(it, timerVariables, buildPoint)
                        }
                    }
                    SKIP_TIMER -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            skipTimer(it, timerVariables, buildPoint)
                        }
                    }


                    SET_VARIABLE -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            setVariable(it, buildPoint, varVariables)
                        }
                    }

                    INCREMENT_VARIABLE -> {
                        if (buildPoint.currentRelativePosition + 1000L >= it.offset) {
                            incrementVariable(it, buildPoint, varVariables)
                        }
                    }


                    SHOW_TIMELINE_MARKER -> {
                        it.toTimelineMarkerEntity()?.let { timelineMarkerEntity ->
                            timelineMarkers.add(timelineMarkerEntity)
                        }
                    }

                    DELETE_ACTION -> {
                        // do nothing
                    }

                    UNKNOWN -> {
                        // should not happen
                    }
                }
            }

        } else {
            sortedActionList.forEach {
                if (it.absoluteTime == -1L) {
                    return@forEach
                }
                val isInGap =
                    buildPoint.player.isWithinValidSegment(it.absoluteTime)?.not() ?: false
                when (it.type) {
                    SHOW_OVERLAY -> {
                        val act =
                            ShowOverlayActionHelper.getOverlayActionCurrentAct(
                                TimeSystem.ABSOLUTE,
                                buildPoint.currentRelativePosition,
                                it,
                                buildPoint.isInterrupted
                            )
                        showOverlay(it, act, buildPoint)

                    }
                    HIDE_OVERLAY -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            annotationListener.removeOverlay(it.toOverlayEntity()!!)
                        }
                    }

                    CREATE_TIMER -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            createTimer(it, timerVariables)
                        }
                    }
                    START_TIMER -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            startTimer(it, timerVariables, buildPoint)
                        }
                    }
                    PAUSE_TIMER -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            pauseTimer(it, timerVariables, buildPoint)
                        }
                    }
                    ADJUST_TIMER -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            adjustTimer(it, timerVariables, buildPoint)
                        }
                    }
                    SKIP_TIMER -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            skipTimer(it, timerVariables, buildPoint)
                        }
                    }

                    SHOW_TIMELINE_MARKER -> TODO()

                    SET_VARIABLE -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            setVariable(it, buildPoint, varVariables)
                        }
                    }
                    INCREMENT_VARIABLE -> {
                        if (buildPoint.currentAbsolutePosition + 1 >= it.absoluteTime) {
                            incrementVariable(it, buildPoint, varVariables)
                        }
                    }

                    UNKNOWN,
                    DELETE_ACTION -> {
                        // do nothing
                    }
                }

            }

        }

        variableKeeper.notifyTimers(timerVariables)
        variableKeeper.notifyVariables(varVariables)

        annotationListener.setTimelineMarkers(timelineMarkers)

        atomicInt.decrementAndGet()
        busyCondition.signal()
        lock.unlock()

    }

    private fun hideOverlay(
        actionObject: ActionObject,
        overlayActionCurrentAct: HideOverlayAct
    ) {
        when (overlayActionCurrentAct) {
            HideOverlayAct.DO_NOTHING -> {
                // do nothing
            }
            HideOverlayAct.OUTRO -> {
                annotationListener.removeOverlay(actionObject.toOverlayEntity()!!)
            }
        }
    }

    private fun showOverlay(
        actionObject: ActionObject,
        act: OverlayAct,
        buildPoint: BuildPoint
    ) {
        when (act) {
            INTRO -> {
                annotationListener.addOverlay(actionObject.toOverlayEntity()!!)
            }
            OUTRO,
            REMOVE -> {
                annotationListener.removeOverlay(actionObject.toOverlayEntity()!!)
            }
            DO_NOTHING -> {
                // do nothing
            }
            LINGERING_INTRO -> {
                val overlayEntity = actionObject.toOverlayEntity()!!
                annotationListener.addOrUpdateLingeringIntroOverlay(
                    overlayEntity,
                    buildPoint.currentRelativePosition - overlayEntity.introTransitionSpec.offset,
                    buildPoint.isPlaying
                )

            }
            LINGERING_MIDWAY -> {
                annotationListener.addOrUpdateLingeringMidwayOverlay(
                    actionObject.toOverlayEntity()!!
                )

            }
            LINGERING_OUTRO -> {
                val overlayEntity = actionObject.toOverlayEntity()!!
                annotationListener.addOrUpdateLingeringOutroOverlay(
                    overlayEntity,
                    buildPoint.currentRelativePosition - (overlayEntity.introTransitionSpec.offset + overlayEntity.outroTransitionSpec.animationDuration),
                    buildPoint.isPlaying
                )
            }
            LINGERING_REMOVE -> {
                annotationListener.removeLingeringOverlay(actionObject.toOverlayEntity()!!)
            }

        }
    }

    private fun incrementVariable(
        actionObject: ActionObject,
        buildPoint: BuildPoint,
        varVariables: HashMap<String, SetVariableEntity>
    ) {
        actionObject.toIncrementVariableEntity()?.let { incrementVariableEntity ->
            val act =
                VariableActionHelper.getIncrementVariableCurrentAct(
                    buildPoint.currentRelativePosition,
                    incrementVariableEntity
                )
            when (act) {
                IncrementVariableCurrentAct.INCREMENT -> {
                    varVariables[incrementVariableEntity.name]?.let { setVariableEntity ->
                        ActionVariableHelper.incrementVariable(
                            setVariableEntity.variable,
                            incrementVariableEntity
                        )

                    }
                }
                IncrementVariableCurrentAct.DO_NOTHING -> {
                    // do nothing
                }
            }
        }
    }

    private fun setVariable(
        actionObject: ActionObject,
        buildPoint: BuildPoint,
        varVariables: HashMap<String, SetVariableEntity>
    ) {
        actionObject.toSetVariable()?.let { setVariableEntity ->
            val act = VariableActionHelper.getVariableCurrentAct(
                buildPoint.currentRelativePosition,
                setVariableEntity
            )
            when (act) {
                VariableAct.CREATE_VARIABLE -> {
                    variableKeeper.createVariablePublisher(setVariableEntity.variable.name)
                    varVariables[setVariableEntity.variable.name] =
                        setVariableEntity
                }
                VariableAct.CLEAR -> {
                }
            }
        }
    }

    private fun skipTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toSkipTimerEntity()?.let { skipTimerEntity ->
            timerVariables[skipTimerEntity.name]?.let { timerVariable ->
                timerVariable.skip(
                    skipTimerEntity,
                    buildPoint.currentRelativePosition
                )
            }
        }
    }

    private fun adjustTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toAdjustTimerEntity()?.let { adjustTimerEntity ->
            timerVariables[adjustTimerEntity.name]?.let { timerVariable ->
                timerVariable.adjust(
                    adjustTimerEntity,
                    buildPoint.currentRelativePosition
                )
            }
        }
    }

    private fun pauseTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toPauseTimerEntity()?.let { pauseTimerEntity ->
            timerVariables[pauseTimerEntity.name]?.let { timerVariable ->
                timerVariable.pause(
                    pauseTimerEntity,
                    buildPoint.currentRelativePosition
                )
            }
        }
    }

    private fun startTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toStartTimerEntity()?.let { startTimerEntity ->
            timerVariables[startTimerEntity.name]?.let { timerVariable ->
                timerVariable.start(
                    startTimerEntity,
                    buildPoint.currentRelativePosition
                )
            }
        }
    }

    private fun createTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>
    ) {
        actionObject.toCreateTimerEntity()?.let { createTimerEntity ->
            variableKeeper.createTimerPublisher(createTimerEntity.name)

            timerVariables[createTimerEntity.name] =
                TimerVariable(
                    createTimerEntity.name,
                    createTimerEntity.format,
                    createTimerEntity.direction,
                    createTimerEntity.startValue,
                    createTimerEntity.capValue
                )
        }
    }

}