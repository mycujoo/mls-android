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
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AnnotationMediator @Inject constructor(
    private val annotationFactory: IAnnotationFactory,
    private val dataManager: IDataManager,
    private val dispatcher: CoroutineScope,
    private val scheduler: ScheduledExecutorService,
    private val logger: Logger,
    private val player: IPlayer
) : IAnnotationMediator {

    // TODO: Hook this up :)
    private lateinit var playerView: MLSPlayerView

    /**region Fields*/
    private lateinit var eventListener: Player.Listener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    fun initialize(handler: Handler) {

        initEventListener(player)

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                val currentPosition = player.currentPosition()
                annotationFactory.attachPlayerView(playerView)
                annotationFactory.build(
                    BuildPoint(
                        currentPosition,
                        player.currentAbsoluteTime(),
                        player,
                        player.isPlaying()
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
        annotationFactory.setActions(actionResponse.data.map { it.toAction() })
    }

    private fun initEventListener(player: IPlayer) {
        eventListener = object : Player.Listener {

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                val time = player.currentPosition()

                if (reason == DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }

                playerView.updateTime(time, player.duration())
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, playbackState)

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
                            player.isPlaying()
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
    override fun setLocalActions(actions: List<Action>) {
        annotationFactory.setLocalActions(actions)
    }

    override fun release() {
        scheduler.shutdown()
    }

    override var onSizeChangedCallback = {
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