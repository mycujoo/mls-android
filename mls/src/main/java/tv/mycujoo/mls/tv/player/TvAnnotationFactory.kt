package tv.mycujoo.mls.tv.player

import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.TvOverlayAct
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.helper.ShowOverlayActionHelper

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
                        ShowOverlayActionHelper.getTVOverlayActionCurrentAct(
                            currentPosition,
                            it,
                            false
                        )
                    when (act) {
                        TvOverlayAct.DO_NOTHING -> {
                            // do nothing
                        }
                        TvOverlayAct.INTRO -> {
                            tvAnnotationListener.addOverlay(it.toOverlayEntity()!!)
                        }
                        TvOverlayAct.OUTRO -> {
                        }
                        TvOverlayAct.REMOVE -> {
                            tvAnnotationListener.removeOverlay(it.toOverlayEntity()!!)
                        }

                    }
                }
                HIDE_OVERLAY -> TODO()
                SHOW_TIMELINE_MARKER -> TODO()
                SET_VARIABLE -> TODO()
                INCREMENT_VARIABLE -> TODO()
                CREATE_TIMER -> TODO()
                START_TIMER -> TODO()
                PAUSE_TIMER -> TODO()
                ADJUST_TIMER -> TODO()
                SKIP_TIMER -> TODO()
            }
        }
    }
}