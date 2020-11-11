package tv.mycujoo.mls.tv.player

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.IncrementVariableCurrentAct
import tv.mycujoo.domain.entity.IncrementVariableCurrentAct.INCREMENT
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.VariableAct.CLEAR
import tv.mycujoo.domain.entity.VariableAct.CREATE_VARIABLE
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.entity.CreateTimerEntity
import tv.mycujoo.mls.helper.ShowOverlayActionHelper
import tv.mycujoo.mls.helper.VariableActionHelper
import tv.mycujoo.mls.helper.VariableActionHelper.Companion.getIncrementVariableCurrentAct
import tv.mycujoo.mls.manager.ITimerHolder
import tv.mycujoo.mls.manager.TimerKeeper
import tv.mycujoo.mls.manager.contracts.TimerHelper
import tv.mycujoo.mls.model.MutablePair

class TvAnnotationFactory(
    private val tvAnnotationListener: TvAnnotationListener,
    private val timerKeeper: TimerKeeper,
    private val timerHolder: ITimerHolder
) {

    private lateinit var sortedActionList: List<ActionObject>

    fun setAnnotations(actionObjectList: List<ActionObject>) {
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

        val timers: ArrayList<Set<MutablePair<CreateTimerEntity, String>>> = ArrayList()

        sortedActionList.forEach() {
            if (currentPosition + 1000L < it.offset) {
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
                    if (currentPosition + 1000L > it.offset) {
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
                        timerKeeper.createTimer(createTimerEntity)
                        val set = setOf(
                            MutablePair(
                                createTimerEntity,
                                createTimerEntity.startValue.toString()
                            )
                        )
                        timers.add(set)
                    }

                }
                START_TIMER -> {
                    it.toStartTimerEntity()?.let { startTimerEntity ->
                        timerKeeper.startTimer(startTimerEntity, currentPosition)
                        timerKeeper.notify(startTimerEntity.name)

                        timers.forEach { set ->
                            set.filter { p -> p.first.name == (startTimerEntity.name) }
                                .forEach { pair ->
                                    pair.second = TimerHelper.start(startTimerEntity, pair, currentPosition)
                                }
                        }
                    }
                }
                PAUSE_TIMER -> {
                    it.toPauseTimerEntity()?.let { pauseTimerEntity ->
                        timerKeeper.pauseTimer(pauseTimerEntity, currentPosition)

                        timers.forEach { set ->
                            set.filter { p -> p.first.name == (pauseTimerEntity.name) }
                                .forEach { pair ->
                                    pair.second = TimerHelper.pause(pauseTimerEntity, pair, currentPosition)
                                }
                        }

                    }
                }
                ADJUST_TIMER -> {
                    it.toAdjustTimerEntity()?.let { adjustTimerEntity ->

                        timers.forEach { set ->
                            set.filter { p -> p.first.name == (adjustTimerEntity.name) }
                                .forEach { pair ->
                                    pair.second = TimerHelper.adjust(adjustTimerEntity, pair, currentPosition)
                                }
                        }
                    }
                }
                SKIP_TIMER -> TODO()
            }
        }

        timerHolder.notifyTimers(timers)
    }
}