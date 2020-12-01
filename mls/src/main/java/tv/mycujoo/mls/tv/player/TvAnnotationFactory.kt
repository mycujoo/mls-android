package tv.mycujoo.mls.tv.player

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.IncrementVariableCurrentAct
import tv.mycujoo.domain.entity.IncrementVariableCurrentAct.INCREMENT
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.VariableAct.CLEAR
import tv.mycujoo.domain.entity.VariableAct.CREATE_VARIABLE
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.core.BuildPoint
import tv.mycujoo.mls.core.IAnnotationFactory
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.helper.ShowOverlayActionHelper
import tv.mycujoo.mls.helper.VariableActionHelper
import tv.mycujoo.mls.helper.VariableActionHelper.Companion.getIncrementVariableCurrentAct
import tv.mycujoo.mls.manager.IVariableKeeper
import tv.mycujoo.mls.manager.TimerVariable

class TvAnnotationFactory(
    private val tvAnnotationListener: TvAnnotationListener,
    private val variableKeeper: IVariableKeeper
) : IAnnotationFactory {

    private lateinit var sortedActionList: List<ActionObject>

    override fun build(buildPoint: BuildPoint) {
        TODO("Not yet implemented")
    }

    override fun actionList(): List<ActionObject> {
        return sortedActionList
    }

    override fun setAnnotations(actionObjectList: List<ActionObject>) {
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
    }

    fun build(currentPosition: Long, isPlaying: Boolean, interrupted: Boolean) {
        if (this::sortedActionList.isInitialized.not()) {
            return
        }
        val timerVariables: HashMap<String, TimerVariable> = HashMap()


        sortedActionList.forEach() {
            if (currentPosition + ONE_SECOND_IN_MS < it.offset) {
                return@forEach
            }
            when (it.type) {
                UNKNOWN -> {
                    // should not happen
                }
                DELETE_ACTION -> {
                    // should not happen. Handled in setAnnotations()
                }
                SHOW_OVERLAY -> {
                    val act =
                        ShowOverlayActionHelper.getOverlayActionCurrentAct(
                            currentPosition,
                            it,
                            interrupted
                        )
                    when (act) {
                        DO_NOTHING -> {
                            // do nothing
                        }
                        INTRO -> {
                            tvAnnotationListener.addOverlay(it.toOverlayEntity()!!)
                        }
                        OUTRO,
                        REMOVE -> {
                            tvAnnotationListener.removeOverlay(it.toOverlayEntity()!!)
                        }
                        LINGERING_INTRO -> {
                            tvAnnotationListener.addOrUpdateLingeringIntroOverlay(
                                it.toOverlayEntity()!!,
                                currentPosition - it.toOverlayEntity()!!.introTransitionSpec.offset,
                                isPlaying
                            )
                        }
                        LINGERING_MIDWAY -> {
                            tvAnnotationListener.addOrUpdateLingeringMidwayOverlay(
                                it.toOverlayEntity()!!
                            )
                        }
                        LINGERING_OUTRO -> {
                            tvAnnotationListener.addOrUpdateLingeringOutroOverlay(
                                it.toOverlayEntity()!!,
                                currentPosition - (it.toOverlayEntity()!!.outroTransitionSpec.offset + it.toOverlayEntity()!!.outroTransitionSpec.animationDuration),
                                isPlaying
                            )
                        }
                        LINGERING_REMOVE -> {
                            tvAnnotationListener.removeLingeringOverlay(it.toOverlayEntity()!!)
                        }
                    }
                }
                HIDE_OVERLAY -> {
                    if (currentPosition + ONE_SECOND_IN_MS > it.offset) {
                        tvAnnotationListener.removeOverlay(it.toHideOverlayActionEntity()!!)
                    }

                }
                SHOW_TIMELINE_MARKER -> TODO()
                SET_VARIABLE -> {
                    it.toSetVariable()?.let { setVariableEntity ->
                        val act = VariableActionHelper.getVariableCurrentAct(
                            currentPosition,
                            setVariableEntity
                        )
                        when (act) {
                            CREATE_VARIABLE -> {
                                tvAnnotationListener.createVariable(setVariableEntity)
                            }
                            CLEAR -> {
                            }
                        }
                    }
                }
                INCREMENT_VARIABLE -> {
                    it.toIncrementVariableEntity()?.let { incrementVariableEntity ->
                        val act =
                            getIncrementVariableCurrentAct(currentPosition, incrementVariableEntity)
                        when (act) {
                            INCREMENT -> {
                                tvAnnotationListener.incrementVariable(incrementVariableEntity)
                            }
                            IncrementVariableCurrentAct.DO_NOTHING -> {
                                // do nothing
                            }
                        }
                    }

                }
                CREATE_TIMER -> {
                    it.toCreateTimerEntity()?.let { createTimerEntity ->
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
                START_TIMER -> {
                    it.toStartTimerEntity()?.let { startTimerEntity ->
                        timerVariables[startTimerEntity.name]?.let { timerVariable ->
                            timerVariable.start(startTimerEntity, currentPosition)
                        }
                    }
                }
                PAUSE_TIMER -> {
                    it.toPauseTimerEntity()?.let { pauseTimerEntity ->
                        timerVariables[pauseTimerEntity.name]?.let { timerVariable ->
                            timerVariable.pause(pauseTimerEntity, currentPosition)
                        }
                    }
                }
                ADJUST_TIMER -> {
                    it.toAdjustTimerEntity()?.let { adjustTimerEntity ->
                        timerVariables[adjustTimerEntity.name]?.let { timerVariable ->
                            timerVariable.adjust(adjustTimerEntity, currentPosition)
                        }
                    }
                }
                SKIP_TIMER -> {
                    it.toSkipTimerEntity()?.let { skipTimerEntity ->
                        timerVariables[skipTimerEntity.name]?.let { timerVariable ->
                            timerVariable.skip(skipTimerEntity, currentPosition)
                        }
                    }
                }
            }
        }

        variableKeeper.notifyTimers(timerVariables)
    }
}