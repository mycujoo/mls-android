package tv.mycujoo.mls.core

import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.domain.entity.IncrementVariableEntity
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.domain.entity.OverlayEntity
import tv.mycujoo.domain.entity.OverlayObject
import tv.mycujoo.domain.entity.SetVariableEntity
import tv.mycujoo.mls.helper.ActionVariableHelper
import tv.mycujoo.mls.helper.AnimationClassifierHelper.Companion.hasOutroAnimation
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.manager.TimerProcessor
import tv.mycujoo.mls.manager.ViewIdentifierManager

class ActionBuilder(
    private val listener: IAnnotationListener,
    private val downloaderClient: DownloaderClient,
    private val viewIdentifierManager: ViewIdentifierManager
) : IActionBuilder() {

    /**region Fields*/
    private var currentTime: Long = 0L
    private var isPlaying: Boolean = false

    private var overlayObjects = ArrayList<OverlayObject>()
    private var toBeDownloadedSvgList = ArrayList<String>()

    private var setVariableActions = ArrayList<SetVariableEntity>()
    private var incrementVariableActions = ArrayList<IncrementVariableEntity>()

    private var appliedSetVariableActions = ArrayList<SetVariableEntity>()

    private lateinit var actionCollections: ActionCollections
    private var appliedCreateTimer = ArrayList<String>()

    private var overlayEntityList = ArrayList<OverlayEntity>()

    private lateinit var timerProcessor: TimerProcessor


    /**endregion */


    override fun addOverlayObjects(overlayObject: List<OverlayObject>) {
        overlayObjects.addAll(overlayObject)
        overlayObject.forEach {
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
            viewIdentifierManager.timeKeeper,
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
        listener.clearScreen(overlayObjects.map { it.id })

        toBeDownloadedSvgList.clear()
        appliedSetVariableActions.clear()
    }

    override fun removeLeftOvers() {
        overlayObjects.forEach { overlayObject ->
            if (isNotDownloading(overlayObject) &&
                isAttached(overlayObject) &&
                shouldNotBeOnScreen(overlayObject)
            ) {
                listener.removeOverlay(overlayObject.toOverlayEntity())
            }
        }
    }

    override fun computeVariableNameValueTillNow() {
        val variablesTillNow = ActionVariableHelper.buildVariablesTillNow(
            currentTime,
            setVariableActions,
            incrementVariableActions
        )
        viewIdentifierManager.variableTranslator.setVariablesNameValueIfDifferent(variablesTillNow)

    }


    override fun processTimers() {
        val toBeNotified = timerProcessor.process(
            currentTime
        )

        toBeNotified.forEach { timerName ->
            viewIdentifierManager.timeKeeper.notify(timerName)
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

    private fun isNotDownloading(overlayObject: OverlayObject): Boolean {
        return toBeDownloadedSvgList.none { it == overlayObject.id }
    }

    private fun isNotAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.overlayObjectIsNotAttached(overlayObject.id)
    }

    private fun isAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.overlayObjectIsAttached(overlayObject.id)
    }

    private fun shouldNotBeOnScreen(overlayObject: OverlayObject): Boolean {
        if (currentTime < overlayObject.introTransitionSpec.offset - 1000) {
            return true
        }

        if (hasOutroAnimation(overlayObject.outroTransitionSpec.animationType)) {
            return currentTime > overlayObject.outroTransitionSpec.offset + overlayObject.outroTransitionSpec.animationDuration
        } else {
            if (overlayObject.outroTransitionSpec.offset != -1L) {
                return currentTime > overlayObject.outroTransitionSpec.offset
            }
            return false
        }
    }

}