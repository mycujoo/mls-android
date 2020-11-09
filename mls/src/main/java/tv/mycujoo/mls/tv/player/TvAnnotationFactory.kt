package tv.mycujoo.mls.tv.player

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.VariableAct.CLEAR
import tv.mycujoo.domain.entity.VariableAct.CREATE_VARIABLE
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.helper.ShowOverlayActionHelper
import tv.mycujoo.mls.helper.VariableActionHelper

class TvAnnotationFactory(private val tvAnnotationListener: TvAnnotationListener) {

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

        sortedActionList.forEach {
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
                        tvAnnotationListener.incrementVariable(incrementVariableEntity)
                    }

                }
                CREATE_TIMER -> TODO()
                START_TIMER -> TODO()
                PAUSE_TIMER -> TODO()
                ADJUST_TIMER -> TODO()
                SKIP_TIMER -> TODO()
            }
        }
    }
}