package tv.mycujoo.mcls.tv.player

import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.enum.MessageLevel
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.utils.ThreadUtils
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TvAnnotationMediator @Inject constructor(
    private val tvAnnotationFactory: IAnnotationFactory,
    private val player: IPlayer,
    private val dispatcher: CoroutineScope,
    private val dataManager: IDataManager,
    private val logger: Logger,
    private val viewHandler: ViewHandler,
    private val threadUtils: ThreadUtils
) {

    private val scheduledRunnable: Runnable
    private val handler = threadUtils.provideHandler()
    private var scheduler = threadUtils.getScheduledExecutorService()

    init {
        player.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    tvAnnotationFactory.build()
                }

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    viewHandler.getAnimations().forEach { it.resume() }
                } else {
                    viewHandler.getAnimations().forEach { it.pause() }
                }
            }
        })

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                tvAnnotationFactory.build()
            }
        }

        scheduledRunnable = Runnable {
            handler.post(exoRunnable)
        }

        initTicker()
    }

    private var hasPendingSeek: Boolean = false

    fun fetchActions(
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

    fun feed(actionResponse: ActionResponse) {
        tvAnnotationFactory.setActions(actionResponse.data.map { it.toAction() })
    }

    fun setLocalActions(actions: List<Action>) {
        tvAnnotationFactory.setLocalActions(actions)
    }


    private fun initTicker() {
        if (scheduler.isShutdown.not()) {
            scheduler.shutdown()
        }
        scheduler = threadUtils.getScheduledExecutorService()

        scheduler.scheduleAtFixedRate(
            scheduledRunnable,
            ONE_SECOND_IN_MS,
            ONE_SECOND_IN_MS,
            TimeUnit.MILLISECONDS
        )
    }

    fun release() {
        scheduler.shutdown()
    }
}
