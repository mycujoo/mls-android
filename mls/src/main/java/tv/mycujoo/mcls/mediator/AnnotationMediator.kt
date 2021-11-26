package tv.mycujoo.mcls.mediator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.core.BuildPoint
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.widgets.MLSPlayerView
import tv.mycujoo.mcls.api.PlayerViewContract
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AnnotationMediator @Inject constructor(
    private val annotationFactory: IAnnotationFactory,
    private val dataManager: IDataManager,
    private val dispatcher: CoroutineScope,
    private val logger: Logger,
    private val player: IPlayer,
    var handler: Handler,
    var scheduler: ScheduledExecutorService
) : IAnnotationMediator {


    private lateinit var playerViewContract: PlayerViewContract

    /**region Fields*/
    private val scheduledRunnable: Runnable

    private lateinit var eventListener: Player.Listener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    init {

        initEventListener(player)

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                val currentPosition = player.currentPosition()
                annotationFactory.attachPlayerView(playerViewContract)
                annotationFactory.build(
                    BuildPoint(
                        currentPosition,
                        player.currentAbsoluteTime(),
                        player,
                        player.isPlaying()
                    )
                )

                val playerView = playerViewContract
                if (playerView is MLSPlayerView) {
                    playerView.updateTime(currentPosition, player.duration())
                }
            }
        }

        scheduledRunnable = Runnable {
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
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
    }

    private fun initEventListener(player: IPlayer) {
        eventListener = object : Player.Listener {

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)

                val time = player.currentPosition()

                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }

                val playerView = playerViewContract
                if (playerView is MLSPlayerView) {
                    playerView.updateTime(time, player.duration())
                }
            }

            /**
             * This Functions on testing, only emits STATE_IDLE. I fixed it by adding onPlaybackStateChanged
             * Which updates every playback state change correctly.
             */
            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, playbackState)

                if (playbackState == STATE_READY) {
                    val playerView = playerViewContract
                    if (playerView is MLSPlayerView) {
                        playerView.updateTime(player.currentPosition(), player.duration())
                    }
                }

                if (playbackState == STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    annotationFactory.build(
                        BuildPoint(
                            player.currentPosition(),
                            player.currentAbsoluteTime(),
                            player,
                            player.isPlaying()
                        )
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)

                if (playbackState == STATE_READY) {
                    val playerView = playerViewContract
                    if (playerView is MLSPlayerView) {
                        playerView.updateTime(player.currentPosition(), player.duration())
                    }
                }

                if (playbackState == STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    annotationFactory.build(
                        BuildPoint(
                            player.currentPosition(),
                            player.currentAbsoluteTime(),
                            player,
                            player.isPlaying()
                        )
                    )
                }
            }
        }
        player.addListener(eventListener)
    }

    override fun initPlayerView(playerView: PlayerViewContract) {
        scheduler = Executors.newScheduledThreadPool(1)
        this.playerViewContract = playerView

        val contract = playerViewContract
        if (contract is MLSPlayerView) {
            contract.setOnSizeChangedCallback(onSizeChangedCallback)
        }
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun setLocalActions(actions: List<Action>) {
        annotationFactory.setLocalActions(actions)
    }

    override fun release() {
        scheduler.shutdown()
    }

    override var onSizeChangedCallback = {
        annotationFactory.attachPlayerView(playerViewContract)
        annotationFactory.build(
            BuildPoint(
                player.currentPosition(),
                player.currentAbsoluteTime(),
                player,
                player.isPlaying()
            )
        )
    }

    /**endregion */
}