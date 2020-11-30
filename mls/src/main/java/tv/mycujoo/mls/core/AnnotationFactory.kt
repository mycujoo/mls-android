package tv.mycujoo.mls.core

import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.helper.*
import tv.mycujoo.mls.manager.TimerVariable
import tv.mycujoo.mls.manager.contracts.IViewHandler
import tv.mycujoo.mls.utils.TimeUtils
import java.util.concurrent.CopyOnWriteArrayList
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

    private var sortedActionList =
        CopyOnWriteArrayList<ActionObject>()// actions, sorted by offset, then by priority
    private var adjustedActionList =
        CopyOnWriteArrayList<ActionObject>()// sortedActionList + adjusted offset time
    private var timeSystem = TimeSystem.RELATIVE

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

        sortedActionList.clear()
        sortedActionList.addAll(sortedTemp.filter { actionObject -> deleteActions.none { actionObject.id == it.id } })


        atomicInt.decrementAndGet()
        busyCondition.signal()
        lock.unlock()
    }

    override fun actionList(): List<ActionObject> {
        return sortedActionList
    }

    override fun build(buildPoint: BuildPoint) {
        val currentTimeInInDvrWindowDuration = TimeRangeHelper.isCurrentTimeInDvrWindowDuration(
            buildPoint.player.duration(),
//            buildPoint.player.dvrWindowSize()
            Long.MAX_VALUE // todo! This should be filled from Stream's dvr-window size value
        )

        if (currentTimeInInDvrWindowDuration) {
            timeSystem = TimeSystem.RELATIVE
            adjustedActionList.clear()
            process(buildPoint, currentTimeInInDvrWindowDuration, sortedActionList)

        } else {
            timeSystem = TimeSystem.ABSOLUTE
            adjustedActionList.clear()
            sortedActionList.forEach {
                adjustedActionList.add(
                    it.copy(
                        offset = TimeUtils.convertRelativeTimeToAbsolute(
                            buildPoint.player.dvrWindowStartTime(),
                            it.absoluteTime
                        )
                    )
                )
            }
            process(buildPoint, currentTimeInInDvrWindowDuration, adjustedActionList)

        }
    }

    private fun process(
        buildPoint: BuildPoint,
        isInDvrWindow: Boolean,
        list: List<ActionObject>
    ) {
        lock.lock()
        if (atomicInt.get() > 0) {
            busyCondition.await()
        }
        atomicInt.incrementAndGet()

        val timerVariables: HashMap<String, TimerVariable> = HashMap()
        val varVariables: HashMap<String, SetVariableEntity> = HashMap()

        val timelineMarkers = ArrayList<TimelineMarkerEntity>()
        list.forEach {
            val isInGap =
                buildPoint.player.isWithinValidSegment(it.absoluteTime)?.not() ?: false
            when (it.type) {
                SHOW_OVERLAY -> {
                    if (isInDvrWindow.not() && isInGap) {
                        return@forEach
                    }
                    val act =
                        ShowOverlayActionHelper.getOverlayActionCurrentAct(
                            buildPoint.currentRelativePosition,
                            it,
                            buildPoint.isInterrupted
                        )
                    showOverlay(it, act, buildPoint)
                }
                HIDE_OVERLAY -> {
                    val act =
                        HideOverlayActionHelper.getOverlayActionCurrentAct(
                            buildPoint.currentRelativePosition,
                            it,
                            buildPoint.isInterrupted
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

                DELETE_ACTION,
                UNKNOWN -> {
                    // should not happen
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
            HideOverlayAct.OUTRO_IN_RANGE -> {
                annotationListener.removeOverlay(actionObject.toOverlayEntity()!!)
            }
            HideOverlayAct.OUTRO_LINGERING -> {
                annotationListener.removeLingeringOverlay(actionObject.toOverlayEntity()!!)
            }
            HideOverlayAct.OUTRO_LEFTOVER -> {
                annotationListener.removeOverlay(actionObject.toHideOverlayActionEntity()!!)
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
            timerVariables[skipTimerEntity.name]?.skip(
                skipTimerEntity,
                buildPoint.currentRelativePosition
            )
        }
    }

    private fun adjustTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toAdjustTimerEntity()?.let { adjustTimerEntity ->
            timerVariables[adjustTimerEntity.name]?.adjust(
                adjustTimerEntity,
                buildPoint.currentRelativePosition
            )
        }
    }

    private fun pauseTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toPauseTimerEntity()?.let { pauseTimerEntity ->
            timerVariables[pauseTimerEntity.name]?.pause(
                pauseTimerEntity,
                buildPoint.currentRelativePosition
            )
        }
    }

    private fun startTimer(
        actionObject: ActionObject,
        timerVariables: HashMap<String, TimerVariable>,
        buildPoint: BuildPoint
    ) {
        actionObject.toStartTimerEntity()?.let { startTimerEntity ->
            timerVariables[startTimerEntity.name]?.start(
                startTimerEntity,
                buildPoint.currentRelativePosition
            )
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