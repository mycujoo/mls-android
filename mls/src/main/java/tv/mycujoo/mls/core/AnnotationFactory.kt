package tv.mycujoo.mls.core

import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.Variable
import tv.mycujoo.domain.entity.VariableType
import tv.mycujoo.domain.entity.models.ActionType.*
import tv.mycujoo.mls.helper.HideOverlayActionHelper
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.helper.ShowOverlayActionHelper
import tv.mycujoo.mls.manager.contracts.IViewHandler

class AnnotationFactory(
    private val annotationListener: IAnnotationListener,
    private val downloaderClient: IDownloaderClient,
    private val viewHandler: IViewHandler
) : IAnnotationFactory {

    private val timerKeeper = viewHandler.getTimerKeeper()

    private lateinit var sortedActionList: List<ActionObject>

    override fun setAnnotations(annotationList: ActionResponse) {
        sortedActionList =
            annotationList.data.map { it.toActionObject() }
                .sortedWith(compareBy<ActionObject> { it.offset }.thenByDescending { it.priority })
    }

    override fun actionList(): List<ActionObject> {
        return sortedActionList
    }

    override fun build(currentPosition: Long, isPlaying: Boolean, interrupted: Boolean) {

        val variables = mutableSetOf<Variable>()

        sortedActionList.forEach {
            when (it.type) {
                SHOW_OVERLAY -> {

                    val act =
                        ShowOverlayActionHelper.getOverlayActionCurrentAct(currentPosition, it, interrupted)

                    when (act) {
                        INTRO -> {
                            downloaderClient.download(it.toOverlayEntity()!!) { overlayEntityWithSvgData ->
                                annotationListener.addOverlay(
                                    overlayEntityWithSvgData
                                )
                            }
                        }
                        OUTRO -> {
                            annotationListener.removeOverlay(it.toOverlayEntity()!!)
                        }
                        DO_NOTHING -> {
                            // do nothing
                        }
                        LINGERING_INTRO -> {
                            downloaderClient.download(it.toOverlayEntity()!!) {
                                annotationListener.addOrUpdateLingeringIntroOverlay(
                                    it,
                                    currentPosition - it.introTransitionSpec.offset,
                                    isPlaying
                                )
                            }
                        }
                        LINGERING_MIDWAY -> {
                            if (interrupted.not()) {
                                return@forEach
                            }
                            downloaderClient.download(it.toOverlayEntity()!!) {
                                annotationListener.addOrUpdateLingeringMidwayOverlay(
                                    it
                                )
                            }
                        }
                        LINGERING_OUTRO -> {
                            downloaderClient.download(it.toOverlayEntity()!!) {
                                annotationListener.addOrUpdateLingeringOutroOverlay(
                                    it,
                                    currentPosition - (it.introTransitionSpec.offset + it.outroTransitionSpec.animationDuration),
                                    isPlaying
                                )
                            }
                        }
                        LINGERING_REMOVE -> {
                            annotationListener.removeLingeringOverlay(it.toOverlayEntity()!!)
                        }

                    }
                }
                HIDE_OVERLAY -> {
                    val act =
                        HideOverlayActionHelper.getOverlayActionCurrentAct(currentPosition, it)

                    when (act) {
                        OUTRO -> {
                            annotationListener.removeOverlay(it.toOverlayEntity()!!)
                        }
                        DO_NOTHING -> {
                            // do nothing
                        }
                        INTRO,
                        LINGERING_INTRO,
                        LINGERING_MIDWAY,
                        LINGERING_OUTRO,
                        LINGERING_REMOVE -> {
                            // should not happen
                        }
                    }
                }
                CREATE_TIMER -> {
                    it.toCreateTimerEntity()?.let { createTimerEntity ->
                        timerKeeper.createTimer(createTimerEntity)
                    }
                }
                START_TIMER -> {
                    it.toStartTimerEntity()?.let { startTimerEntity ->
                        timerKeeper.startTimer(startTimerEntity, currentPosition)
                        timerKeeper.notify(startTimerEntity.name)
                    }
                }
                PAUSE_TIMER -> {
                    it.toPauseTimerEntity()?.let { pauseTimerEntity ->
                        timerKeeper.pauseTimer(pauseTimerEntity, currentPosition)
                    }
                }
                ADJUST_TIMER -> {
                    it.toAdjustTimerEntity()?.let { adjustTimerEntity ->
                        timerKeeper.adjustTimer(adjustTimerEntity, currentPosition)
                        timerKeeper.notify(adjustTimerEntity.name)
                    }
                }
                SKIP_TIMER -> {
                    it.toSkipTimerEntity()?.let { skipTimerEntity ->
                        timerKeeper.skipTimer(skipTimerEntity)
                        timerKeeper.notify(skipTimerEntity.name)
                    }
                }


                SET_VARIABLE -> {
                    if (it.offset > currentPosition) {
                        return@forEach
                    }
                    it.toSetVariable()?.let { setVariableEntity ->
                        viewHandler.getVariableTranslator()
                            .createVariableTripleIfNotExisted(setVariableEntity.variable.name)
                        variables.add(setVariableEntity.variable)
                    }
                }

                INCREMENT_VARIABLE -> {
                    if (it.offset > currentPosition) {
                        return@forEach
                    }
                    it.toIncrementVariableEntity()?.let { incrementVariableEntity ->

                        variables.filter { it.name == incrementVariableEntity.name }.forEach {
                            var initialValue = it.value

                            when (it.type) {
                                VariableType.DOUBLE -> {
                                    if (incrementVariableEntity.amount is Double) {
                                        initialValue = (initialValue as Double) + incrementVariableEntity.amount
                                    }
                                    if (incrementVariableEntity.amount is Long) {
                                        initialValue =
                                            (initialValue as Double) + incrementVariableEntity.amount.toDouble()
                                    }
                                }
                                VariableType.LONG -> {
                                    if (incrementVariableEntity.amount is Double) {
                                        initialValue = (initialValue as Long) + incrementVariableEntity.amount.toLong()
                                    }
                                    if (incrementVariableEntity.amount is Long) {
                                        initialValue = (initialValue as Long) + incrementVariableEntity.amount
                                    }
                                }
                                VariableType.STRING,
                                VariableType.UNSPECIFIED -> {
                                    // should not happen
                                }
                            }

                            it.value = initialValue
                        }


                    }
                }


                SHOW_TIMELINE_MARKER -> {
                }

                UNKNOWN -> {
                    // should not happen
                }
            }
        }

        variables.forEach { viewHandler.getVariableTranslator().emitNewValue(it.name, it.value) }
        variables.clear()
    }
}