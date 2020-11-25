package tv.mycujoo.mls.mediator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.entity.models.ActionType
import tv.mycujoo.domain.entity.models.ParsedOverlayRelatedData
import tv.mycujoo.domain.entity.models.ParsedTimerRelatedData
import tv.mycujoo.mls.core.BuildPoint
import tv.mycujoo.mls.core.IAnnotationFactory
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.enum.C
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.enum.MessageLevel
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.model.ScreenTimerDirection
import tv.mycujoo.mls.model.ScreenTimerFormat
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.widgets.MLSPlayerView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class AnnotationMediator(
    private var playerView: MLSPlayerView,
    private val annotationFactory: IAnnotationFactory,
    val dataManager: IDataManager,
    val dispatcher: CoroutineScope,
    player: IPlayer,
    private val scheduler: ScheduledExecutorService,
    handler: Handler,
    private val logger: Logger
) : IAnnotationMediator {

    /**region Fields*/
    private lateinit var eventListener: Player.EventListener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    init {
        initEventListener(player)

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                val currentPosition = player.currentPosition()

                annotationFactory.build(
                    BuildPoint(
                        currentPosition,
                        player.currentAbsoluteTime(),
                        player,
                        player.isPlaying(),
                        false
                    )
                )

                playerView.updateTime(currentPosition, player.duration())
            }
        }

        val scheduledRunnable = Runnable {
            handler.post(exoRunnable)
        }
        scheduler.scheduleAtFixedRate(
            scheduledRunnable,
            ONE_SECOND_IN_MS,
            ONE_SECOND_IN_MS,
            TimeUnit.MILLISECONDS
        )

    }


    override fun fetchActions(
        timelineId: String,
        updateId: String?,
        resultCallback: ((result: Result<Exception, ActionResponse>) -> Unit)?
    ) {
        dispatcher.launch(context = Dispatchers.Main) {
            val result = dataManager.getActions(timelineId, updateId)
            resultCallback?.invoke(result)
            when (result) {
                is Result.Success -> {
                    feed(result.value)
                }
                is Result.NetworkError -> {
                    logger.log(MessageLevel.DEBUG, C.NETWORK_ERROR_MESSAGE.plus("${result.error}"))
                }
                is Result.GenericError -> {
                    logger.log(
                        MessageLevel.DEBUG,
                        C.INTERNAL_ERROR_MESSAGE.plus(" ${result.errorMessage} ${result.errorCode}")
                    )
                }
            }

        }
    }

    override fun feed(actionResponse: ActionResponse) {
        annotationFactory.setAnnotations(actionResponse.data.map { it.toActionObject() })
    }

    private fun initEventListener(player: IPlayer) {
        eventListener = object : Player.EventListener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                val time = player.currentPosition()

                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }

                playerView.updateTime(time, player.duration())
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)

                if (playbackState == STATE_READY) {
                    playerView.updateTime(player.currentPosition(), player.duration())
                }

                if (playbackState == STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    annotationFactory.build(
                        BuildPoint(
                            player.currentPosition(),
                            player.currentAbsoluteTime(),
                            player,
                            player.isPlaying(),
                            true
                        )
                    )
                }
            }

        }
        player.addListener(eventListener)
    }

    override fun initPlayerView(playerView: MLSPlayerView) {
        this.playerView = playerView
        playerView.setOnSizeChangedCallback(onSizeChangedCallback)
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun release() {
        scheduler.shutdown()
    }

    override var onSizeChangedCallback = {
        annotationFactory.build(
            BuildPoint(
                player.currentPosition(),
                player.currentAbsoluteTime(),
                player,
                player.isPlaying(),
                false
            )
        )
    }

    /**endregion */

    @OptIn(ExperimentalStdlibApi::class)
    fun feedTestData() {

// sample data for testing overlays & timer

        val parsedOverlayRelatedData = ParsedOverlayRelatedData(
            "id_0",
            "",
            5000L,
            PositionGuide(5F, null, 5F),
            Pair(33F, -1F),
            AnimationType.SLIDE_FROM_LEFT,
            2000L,
            AnimationType.NONE,
            -1L,
            listOf("${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer")
        )
        val actionObject = ActionObject(
            "id_0",
            ActionType.SHOW_OVERLAY,
            2000L,
            1605609884L,
            parsedOverlayRelatedData,
            null,
            null
        )

        val parsedOverlayRelatedData1 = ParsedOverlayRelatedData(
            "id_1",
            "",
            15000L,
            PositionGuide(5F, null, 5F),
            Pair(33F, -1F),
            AnimationType.SLIDE_FROM_LEFT,
            5000L,
            AnimationType.NONE,
            -1L,
            listOf("${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer")
        )
        val actionObject1 = ActionObject(
            "id_1",
            ActionType.SHOW_OVERLAY,
            4000L,
            1605609886L,
            parsedOverlayRelatedData1,
            null,
            null
        )

        val parsedOverlayRelatedData2 = ParsedOverlayRelatedData(
            "id_2",
            "",
            -1L,
            PositionGuide(null, 5F, 5F),
            Pair(33F, -1F),
            AnimationType.SLIDE_FROM_RIGHT,
            3000L,
            AnimationType.NONE,
            -1L,
            listOf("${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer")
        )
        val actionObject2 = ActionObject(
            "id_2",
            ActionType.SHOW_OVERLAY,
            4000L,
            1605609886L,
            parsedOverlayRelatedData2,
            null,
            null
        )

        val parsedOverlayRelatedData3 = ParsedOverlayRelatedData(
            "id_2",
            "",
            -1L,
            PositionGuide(null, 5F, 5F),
            Pair(33F, -1F),
            AnimationType.NONE,
            -1L,
            AnimationType.SLIDE_TO_RIGHT,
            1000L,
            emptyList()
        )
        val actionObject3 = ActionObject(
            "id_2",
            ActionType.HIDE_OVERLAY,
            9000L,
            1605609891L,
            parsedOverlayRelatedData3,
            null,
            null
        )

        val parsedOverlayRelatedData4 = ParsedOverlayRelatedData(
            "id_4",
            "",
            50000L,
            PositionGuide(5F, null, 30F, null),
            Pair(33F, 10F),
            AnimationType.SLIDE_FROM_LEFT,
            30000L,
            AnimationType.NONE,
            -1L,
            listOf("${"$"}awayScore", "${"$"}homeScore", "${"$"}scoreboardTimer")
        )
        val actionObject4 = ActionObject(
            "id_4",
            ActionType.SHOW_OVERLAY,
            5000L,
            1605609887L,
            parsedOverlayRelatedData4,
            null,
            null
        )

        val dataMap = buildMap<String, Any> {
            put("name", "${"$"}awayScore")
            put("value", 5L)
            put("type", "long")
            put("double_precision", 2)
        }
        val setVariableActionObject = ActionObject(
            "id_5",
            ActionType.SET_VARIABLE,
            1000L,
            1605609883L,
            null,
            null,
            dataMap
        )

        val dataMap2 = buildMap<String, Any> {
            put("name", "${"$"}awayScore")
            put("amount", 5L)
        }
        val increaseVariableActionObject = ActionObject(
            "id_6",
            ActionType.INCREMENT_VARIABLE,
            5000L,
            1605609887L,
            null,
            null,
            dataMap2
        )

        val parsedTimerRelatedData = ParsedTimerRelatedData(
            "${"$"}scoreboardTimer",
            ScreenTimerFormat.MINUTES_SECONDS,
            ScreenTimerDirection.UP,
            5000L,
            1000L,
            -1L,
            0L
        )
        val createTimerActionObject = ActionObject(
            "id_7",
            ActionType.CREATE_TIMER,
            1000L,
            1605609883L,
            null,
            parsedTimerRelatedData,
            null
        )
        val startTimerDataMap = buildMap<String, Any> {
            put("name", "${"$"}scoreboardTimer")
        }
        val startTimerActionObject =
            ActionSourceData("id_8", "start_timer", 3000L, 1605609885L, startTimerDataMap).toActionObject()

        val pauseTimerDataMap = buildMap<String, Any> {
            put("name", "${"$"}scoreboardTimer")
        }
        val pauseTimerActionObject =
            ActionSourceData("id_8", "pause_timer", 12000L, 1605609894L, pauseTimerDataMap).toActionObject()

        annotationFactory.setAnnotations(
            listOf(
                actionObject,
                actionObject1,
                actionObject2,
                actionObject3,
                actionObject4,
                setVariableActionObject,
                increaseVariableActionObject,
                createTimerActionObject,
                startTimerActionObject,
                pauseTimerActionObject
            )
        )
    }

}