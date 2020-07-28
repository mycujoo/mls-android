package tv.mycujoo.mls.core

import android.util.Log
import okhttp3.*
import tv.mycujoo.data.entity.ActionCollections
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.AnimationType.*
import tv.mycujoo.domain.entity.OverlayAct.*
import tv.mycujoo.mls.helper.ActionVariableHelper
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.widgets.CreateTimerEntity
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ActionBuilderImpl(
    private val listener: AnnotationListener,
    private val okHttpClient: OkHttpClient,
    private val viewIdentifierManager: ViewIdentifierManager
) : ActionBuilder() {

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


    /**endregion */


    // re-write
    override fun addOverlayObjects(overlayObject: List<OverlayObject>) {
        overlayObjects.addAll(overlayObject)
        overlayObject.forEach {
            overlayEntityList.add(it.toOverlayEntity())
        }

    }

    override fun addSetVariableEntities(setVariables: List<SetVariableEntity>) {
        setVariableActions.addAll(setVariables)
    }

    override fun addIncrementVariableEntities(incrementVariables: List<IncrementVariableEntity>) {
        incrementVariableActions.addAll(incrementVariables)
    }

    override fun addActionCollections(actionCollections: ActionCollections) {
        this.actionCollections = actionCollections
    }

    override fun buildCurrentTimeRange() {

        // todo [WIP]
        overlayEntityList.forEach { overlayEntity ->
            val act = overlayEntity.update(currentTime)
            when (act) {
                DO_NOTHING -> {
                    // do nothing
                }
                INTRO -> {
                    downloadSVGThenCallListener(overlayEntity) { listener.addOverlay(it) }
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

        // timer todo [WIP]
        if (this::actionCollections.isInitialized) {
            actionCollections.createTimerEntityList
                .forEach {
                    if (isInCurrentTimeRange(it.offset) && !isTimerCreated(it.name)) {
                        createTimer(it)
                    } else {
                        if (shouldBeKilled(it)) {
                            clearTimer(it)
                        }
                    }
                }

            actionCollections.startTimerEntityList.forEach {
                if (isInCurrentTimeRange(it.offset) && isTimerCreated(it.name)) {
                    startTimer(it.name)
                }
            }

        }

    }

    override fun buildLingerings() {

        overlayEntityList.forEach { overlayEntity ->
            when (overlayEntity.forceUpdate(currentTime)) {
                LINGERING_INTRO -> {

                    downloadSVGThenCallListener(overlayEntity) {
                        listener.addOrUpdateLingeringIntroOverlay(
                            it,
                            currentTime - it.introTransitionSpec.offset,
                            isPlaying
                        )
                    }

                }
                LINGERING_MIDWAY -> {

                    downloadSVGThenCallListener(overlayEntity) {
                        listener.addOrUpdateLingeringMidwayOverlay(
                            it
                        )
                    }

                }
                LINGERING_OUTRO -> {

                    downloadSVGThenCallListener(overlayEntity) {
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

    override fun recalculateTimers() {

        appliedCreateTimer.clear()

        if (this::actionCollections.isInitialized) {
            actionCollections.createTimerEntityList.forEach {
                if (isUpUntilNow(it.offset) && !isTimerCreated(it.name)) {
                    createTimer(it)
                }
            }

            appliedCreateTimer.forEach { appliedCreateTimer ->


                actionCollections.timerEntity.firstOrNull { it.name == appliedCreateTimer }
                    ?.let { timerEntity ->

                        // start or pause
                        val lastStartTimerEntity =
                            timerEntity.startCommand.filter { it.name == appliedCreateTimer }
                                .maxBy { it.offset }

                        val lastPauseTimerEntity =
                            timerEntity.pauseCommand.filter { it.name == appliedCreateTimer }
                                .maxBy { it.offset }

                        lastStartTimerEntity?.let {
                            if (lastPauseTimerEntity != null && lastPauseTimerEntity.offset > it.offset) {
                                pauseTimer(it.name)
                            } else {
                                startTimer(it.name)
                                viewIdentifierManager.timeKeeper.tuneWithStartEntity(
                                    appliedCreateTimer,
                                    it,
                                    currentTime
                                )
                            }
                        }

                        // adjust
                        timerEntity.adjustCommand.filter { isUpUntilNow(it.offset) }
                            .maxBy {
                                it.offset
                            }?.let {
                                // todo [WIP]
                                viewIdentifierManager.timeKeeper.tuneWithAdjustEntity(
                                    appliedCreateTimer,
                                    it,
                                    currentTime
                                )
                            }


                    }
            }

        }

    }

    /**region Over-ridden Functions*/
    override fun setCurrentTime(time: Long, playing: Boolean) {
//        println("MLS-App AnnotationBuilderImpl - setCurrentTime() $time isPlaying -> $isPlaying")
        currentTime = time
        isPlaying = playing
    }
    /**endregion */

    /**region SetVariableEntity*/
    /**endregion */


    /**region Action classifiers*/

    //re-write
    private fun isNotDownloading(overlayObject: OverlayObject): Boolean {
        return toBeDownloadedSvgList.none { it == overlayObject.id }
    }

    private fun isNotAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.overlayObjectIsNotAttached(overlayObject.id)
    }

    private fun isAttached(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.overlayObjectIsAttached(overlayObject.id)
    }

    private fun isNotInDisplayingAnimation(overlayObject: OverlayObject): Boolean {
        return viewIdentifierManager.hasNoActiveAnimation(overlayObject.id)
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


    /**
     * return true if the action offset is now or in 1 second
     */
    private fun additionIsInCurrentTimeRange(overlayObject: OverlayObject): Boolean {
        return (overlayObject.introTransitionSpec.offset >= currentTime) && (overlayObject.introTransitionSpec.offset < currentTime + 1000L)
    }

    private fun removalIsInCurrentTimeRange(overlayObject: OverlayObject): Boolean {
        if (overlayObject.outroTransitionSpec.animationType == UNSPECIFIED) {
            return false
        }

        return (overlayObject.outroTransitionSpec.offset >= currentTime) && (overlayObject.outroTransitionSpec.offset < currentTime + 1000L)
    }


    private fun isLingeringEndlessOverlay(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        if (overlayObject.outroTransitionSpec.offset == -1L) {
            // it doesn't have ending spec
            return if (hasEnteringAnimation(overlayObject.introTransitionSpec.animationType)) {
                currentTime > overlayObject.introTransitionSpec.offset + overlayObject.introTransitionSpec.animationDuration
            } else {
                currentTime > overlayObject.introTransitionSpec.offset
            }
        }
        return false

    }

    private fun isLingeringExcludingAnimationPart(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        if (overlayObject.outroTransitionSpec.offset == -1L || overlayObject.outroTransitionSpec.animationDuration == 0L) {
            return false
        }

        var leftBound = overlayObject.introTransitionSpec.offset
        var rightBound = 0L

        if (hasEnteringAnimation(overlayObject.introTransitionSpec.animationType)) {
            leftBound =
                overlayObject.introTransitionSpec.offset + overlayObject.introTransitionSpec.animationDuration
        }

        if (hasOutroAnimation(overlayObject.outroTransitionSpec.animationType)) {
            rightBound = overlayObject.outroTransitionSpec.offset
        }

        return (currentTime > leftBound) && (currentTime < rightBound)
    }

    private fun isLingeringInIntroAnimation(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        val leftBound = overlayObject.introTransitionSpec.offset
        val rightBound =
            overlayObject.introTransitionSpec.offset + overlayObject.introTransitionSpec.animationDuration

        return (leftBound <= currentTime) && (currentTime <= rightBound)
    }

    private fun isLingeringInOutroAnimation(overlayObject: OverlayObject): Boolean {
        if (overlayObject.introTransitionSpec.offset > currentTime) {
            return false
        }

        if (overlayObject.outroTransitionSpec.animationDuration == -1L || overlayObject.outroTransitionSpec.animationDuration > currentTime) {
            return false
        }

        val leftBound = overlayObject.outroTransitionSpec.offset
        val rightBound =
            overlayObject.outroTransitionSpec.offset + overlayObject.outroTransitionSpec.animationDuration

        return (leftBound <= currentTime) && (currentTime <= rightBound)
    }

    private fun hasEnteringAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            FADE_IN,
            SLIDE_FROM_LEFT,
            SLIDE_FROM_RIGHT -> {
                true
            }
            else -> false
        }
    }

    private fun hasOutroAnimation(animationType: AnimationType): Boolean {
        return when (animationType) {
            FADE_OUT,
            SLIDE_TO_LEFT,
            SLIDE_TO_RIGHT -> {
                true
            }
            else -> false
        }
    }
    /**endregion */

    /**region Variables classifiers*/
    private fun isNotApplied(setVariableEntity: SetVariableEntity): Boolean {
        return appliedSetVariableActions.none { it == setVariableEntity }
    }

    private fun isInCurrentTimeRange(setVariableEntity: SetVariableEntity): Boolean {
        return (setVariableEntity.offset >= currentTime) && (setVariableEntity.offset < currentTime + 1000L)
    }


    /**endregion */

    /**region Timer classifier and helpers*/
    private fun isInCurrentTimeRange(offset: Long): Boolean {
        return (offset >= currentTime) && (offset < currentTime + 1000L)
    }

    private fun isUpUntilNow(offset: Long): Boolean {
        return offset < currentTime
    }

    private fun isTimerCreated(name: String): Boolean {
        return appliedCreateTimer.any { it == name }
    }

    private fun shouldBeKilled(createTimerEntity: CreateTimerEntity): Boolean {
        if (currentTime < createTimerEntity.offset - 1000) {
            return false
        }
        return true
    }

    private fun createTimer(createTimerEntity: CreateTimerEntity) {
        appliedCreateTimer.add(createTimerEntity.name)
        viewIdentifierManager.timeKeeper.createTimer(createTimerEntity)
    }


    private fun startTimer(name: String) {
        viewIdentifierManager.timeKeeper.startTimer(name)
    }

    private fun pauseTimer(name: String) {
        viewIdentifierManager.timeKeeper.pauseTimer(name)
    }

    private fun clearTimer(createTimerEntity: CreateTimerEntity) {
        appliedCreateTimer.remove(createTimerEntity.name)
    }
    /**endregion */

    /**region msc*/
    private fun downloadSVGThenCallListener(
        overlayEntity: OverlayEntity,
        callback: (OverlayEntity) -> Unit
    ) {
        overlayEntity.isDownloading = true

        val svgUrl = overlayEntity.svgData!!.svgUrl!!
        val request: Request = Request.Builder().url(svgUrl).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                overlayEntity.isDownloading = false

                Log.e("downloadSVGThenCallLis", "downloadSVGThenCallListener() - onFailure()")
            }

            override fun onResponse(call: Call, response: Response) {

                if (response.isSuccessful && response.body() != null) {

                    val stringBuilder = StringBuilder()

                    val scanner = Scanner(response.body()!!.byteStream())
                    while (scanner.hasNext()) {
                        stringBuilder.append(scanner.nextLine())
                    }
                    val svgString = stringBuilder.toString()

                    overlayEntity.isDownloading = false
                    overlayEntity.isOnScreen = true

                    callback(
                        overlayEntity.copy(
                            svgData = SvgData(svgUrl, null, svgString)
                        )
                    )
                }
            }
        })
    }
    /**endregion */

}