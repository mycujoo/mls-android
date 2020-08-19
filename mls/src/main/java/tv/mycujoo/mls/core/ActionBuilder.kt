package tv.mycujoo.mls.core

import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.OverlayBlueprint
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.mls.helper.ActionVariableHelper
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasOutroAnimation
import tv.mycujoo.mls.helper.IDownloaderClient
import tv.mycujoo.mls.manager.TimerProcessor
import tv.mycujoo.mls.manager.contracts.IViewHandler

class ActionBuilder(
    private val listener: IAnnotationListener,
    private val downloaderClient: IDownloaderClient,
    private val viewHandler: IViewHandler
) : IActionBuilder() {

    /**region Fields*/
    private var currentTime: Long = 0L
    private var isPlaying: Boolean = false

    private var overlayBlueprints = ArrayList<OverlayBlueprint>()
    private var toBeDownloadedSvgList = ArrayList<String>()

    private var setVariableActions = ArrayList<SetVariableEntity>()
    private var incrementVariableActions = ArrayList<IncrementVariableEntity>()

    private var appliedSetVariableActions = ArrayList<SetVariableEntity>()

    private lateinit var actionCollections: ActionCollections
    private var appliedCreateTimer = ArrayList<String>()

    private var overlayEntityList = ArrayList<OverlayEntity>()

    private lateinit var timerProcessor: TimerProcessor


    /**endregion */


    override fun addOverlayBlueprints(overlayBlueprint: List<OverlayBlueprint>) {
        overlayBlueprints.addAll(overlayBlueprint)
        overlayBlueprint.forEach {
            overlayEntityList.add(it.toOverlayEntity())
        }
    }

    override fun addOverlayEntities(overlayEntities: List<OverlayEntity>) {
        overlayEntityList.addAll(overlayEntities)
    }

    override fun addSetVariableEntities(setVariables: List<SetVariableEntity>) {
        setVariableActions.addAll(setVariables)
    }

    override fun addIncrementVariableEntities(incrementVariables: List<IncrementVariableEntity>) {
        incrementVariableActions.addAll(incrementVariables)
    }

    override fun addActionCollections(actionCollections: ActionCollections) {
        this.actionCollections = actionCollections
        timerProcessor = TimerProcessor(
            actionCollections.timerCollection,
            viewHandler.getTimeKeeper(),
            appliedCreateTimer
        )
    }

    override fun buildCurrentTimeRange() {

        overlayEntityList.forEach { overlayEntity ->
            val act = overlayEntity.update(currentTime)
            when (act) {
                DO_NOTHING -> {
                    // do nothing
                }
                INTRO -> {
                    downloaderClient.download(overlayEntity) { listener.addOverlay(it) }
                }
                OUTRO -> {
                    listener.removeOverlay(overlayEntity)
                }

                LINGERING_INTRO,
                LINGERING_MIDWAY,
                LINGERING_OUTRO,
                LINGERING_REMOVE -> {
                    // should not happen
                }
            }
        }

    }

    override fun buildLingerings() {

        overlayEntityList.forEach { overlayEntity ->
            when (overlayEntity.forceUpdate(currentTime)) {
                LINGERING_INTRO -> {

                    downloaderClient.download(overlayEntity) {
                        listener.addOrUpdateLingeringIntroOverlay(
                            it,
                            currentTime - it.introTransitionSpec.offset,
                            isPlaying
                        )
                    }

                }
                LINGERING_MIDWAY -> {

                    downloaderClient.download(overlayEntity) {
                        listener.addOrUpdateLingeringMidwayOverlay(
                            it
                        )
                    }

                }
                LINGERING_OUTRO -> {

                    downloaderClient.download(overlayEntity) {
                        listener.addOrUpdateLingeringOutroOverlay(
                            it,
                            currentTime - (overlayEntity.introTransitionSpec.offset + overlayEntity.outroTransitionSpec.animationDuration),
                            isPlaying
                        )
                    }

                }
                LINGERING_REMOVE -> {
                    listener.removeLingeringOverlay(overlayEntity)
                }
                DO_NOTHING,
                INTRO,
                OUTRO -> {
                    // should not happen
                }
            }
        }
    }

    override fun removeAll() {
        listener.clearScreen(overlayBlueprints.map { it.id })

        toBeDownloadedSvgList.clear()
        appliedSetVariableActions.clear()
    }

    override fun removeLeftOvers() {
        overlayBlueprints.forEach { overlayBlueprint ->
            if (isNotDownloading(overlayBlueprint) &&
                isAttached(overlayBlueprint) &&
                shouldNotBeOnScreen(overlayBlueprint)
            ) {
                listener.removeOverlay(overlayBlueprint.toOverlayEntity())
            }
        }
    }

    override fun computeVariableNameValueTillNow() {
        val variablesTillNow = ActionVariableHelper.buildVariablesTillNow(
            currentTime,
            setVariableActions,
            incrementVariableActions
        )
        viewHandler.getVariableTranslator().setVariablesNameValueIfDifferent(variablesTillNow)

    }


    override fun processTimers() {
        val toBeNotified = timerProcessor.process(
            currentTime
        )

        toBeNotified.forEach { timerName ->
            viewHandler.getTimeKeeper().notify(timerName)
        }

    }


    /**region Over-ridden Functions*/
    override fun setCurrentTime(time: Long, playing: Boolean) {
//        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time isPlaying -> $isPlaying")
        currentTime = time
        isPlaying = playing
    }
    /**endregion */


    /**region Action classifiers*/

    private fun isNotDownloading(overlayBlueprint: OverlayBlueprint): Boolean {
        return toBeDownloadedSvgList.none { it == overlayBlueprint.id }
    }

    private fun isNotAttached(overlayBlueprint: OverlayBlueprint): Boolean {
        return viewHandler.overlayBlueprintIsNotAttached(overlayBlueprint.id)
    }

    private fun isAttached(overlayBlueprint: OverlayBlueprint): Boolean {
        return viewHandler.overlayBlueprintIsAttached(overlayBlueprint.id)
    }

    private fun shouldNotBeOnScreen(overlayBlueprint: OverlayBlueprint): Boolean {
        if (currentTime < overlayBlueprint.introTransitionSpec.offset - 1000) {
            return true
        }

        if (hasOutroAnimation(overlayBlueprint.outroTransitionSpec.animationType)) {
            return currentTime > overlayBlueprint.outroTransitionSpec.offset + overlayBlueprint.outroTransitionSpec.animationDuration
        } else {
            if (overlayBlueprint.outroTransitionSpec.offset != -1L) {
                return currentTime > overlayBlueprint.outroTransitionSpec.offset
            }
            return false
        }
    }

}