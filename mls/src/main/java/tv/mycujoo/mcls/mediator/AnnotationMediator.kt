package tv.mycujoo.mcls.mediator

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.api.PlayerViewContract
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.utils.ThreadUtils
import tv.mycujoo.mcls.widgets.MLSPlayerView
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationMediator @Inject constructor(
    private val annotationFactory: IAnnotationFactory,
    private val dataManager: IDataManager,
    private val dispatcher: CoroutineScope,
    private val logger: Logger,
    private val player: IPlayer,
    private val threadUtils: ThreadUtils,
) : IAnnotationMediator {

    private var scheduler = threadUtils.getScheduledExecutorService()
    private val handler = threadUtils.provideHandler()

    private lateinit var playerViewContract: PlayerViewContract

    /**region Fields*/

    private lateinit var eventListener: Listener
    private var hasPendingSeek: Boolean = false
    /**endregion */

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
        annotationFactory.setMCLSActions(actionResponse.data.map { it.toAction() })
    }

    private fun initEventListener(player: IPlayer) {
        eventListener = object : Listener {

            override fun onPositionDiscontinuity(
                oldPosition: PositionInfo,
                newPosition: PositionInfo,
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

                    annotationFactory.build()
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

                    annotationFactory.build()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                if (reason == MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null) {
                    annotationFactory.setActions(emptyList())
                    annotationFactory.build()
                }
            }
        }
        player.addListener(eventListener)
    }

    override fun initPlayerView(playerView: PlayerViewContract) {
        this.playerViewContract = playerView
        if (playerView is MLSPlayerView) {
            annotationFactory.attachPlayerView(playerViewContract)
            initEventListener(player)

            initTicker {
                handler.post {
                    if (player.isPlaying()) {
                        annotationFactory.build()
                        val currentPosition = player.currentPosition()
                        playerView.updateTime(currentPosition, player.duration())
                    }
                }
            }
            playerView.setOnSizeChangedCallback(onSizeChangedCallback)
        }
    }

    private fun initTicker(scheduledRunnable: Runnable) {
        if (scheduler.isShutdown.not()) {
            scheduler.shutdown()
        }
        scheduler = threadUtils.getScheduledExecutorService()

        scheduler.scheduleAtFixedRate(
            scheduledRunnable,
            0, // Initial Delay
            ONE_SECOND_IN_MS,
            TimeUnit.MILLISECONDS
        )
    }
    /**endregion */

    /**region Over-ridden Functions*/
    override fun setLocalActions(actions: List<Action>) {
        annotationFactory.setActions(actions)
    }

    override fun setMCLSActions(actions: List<Action>) {
        annotationFactory.setMCLSActions(actions)
    }

    override fun release() {
        scheduler.shutdown()
    }

    override var onSizeChangedCallback = {
        annotationFactory.attachPlayerView(playerViewContract)
        annotationFactory.build()
    }

    /**endregion */
}