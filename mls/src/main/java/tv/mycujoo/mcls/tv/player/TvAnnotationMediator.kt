package tv.mycujoo.mcls.tv.player

import android.os.Handler
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.mcls.core.AnnotationFactory
import tv.mycujoo.mcls.core.BuildPoint
import tv.mycujoo.mcls.core.IAnnotationFactory
import tv.mycujoo.mcls.core.IAnnotationListener
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.di.TV
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.helper.AnimationFactory
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.OverlayFactory
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.mediator.IAnnotationMediator
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.widgets.MLSPlayerView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TvAnnotationMediator @Inject constructor(
    val tvAnnotationListener: IAnnotationFactory,
    val dataManager: IDataManager,
    val dispatcher: CoroutineScope,
    val scheduler: ScheduledExecutorService,
    val logger: Logger,
    val coroutineScope: CoroutineScope,
    val player: IPlayer
) : IAnnotationMediator {

    // TODO: Hook this up :)
    private lateinit var playerView: MLSPlayerView


    private val variableTranslator = VariableTranslator(coroutineScope)
    private val variableKeeper = VariableKeeper(coroutineScope)

    private var hasPendingSeek: Boolean = false


    override fun initPlayerView(playerView: MLSPlayerView) {
        this.playerView = playerView
        playerView.setOnSizeChangedCallback(onSizeChangedCallback)
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override var onSizeChangedCallback: () -> Unit
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun fetchActions(
        timelineId: String,
        updateId: String?,
        resultCallback: ((result: Result<Exception, ActionResponse>) -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun feed(actionResponse: ActionResponse) {
        TODO("Not yet implemented")
    }

    override fun setLocalActions(actions: List<Action>) {
        TODO("Not yet implemented")
    }

    fun initialize(handler: Handler) {

        player.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    tvAnnotationListener.build(
                        BuildPoint(
                            player.currentPosition(),
                            player.currentAbsoluteTime(),
                            player,
                            player.isPlaying()
                        )
                    )
                }

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                if (isPlaying) {
//                    viewHandler.getAnimations().forEach { it.resume() }
//                } else {
//                    viewHandler.getAnimations().forEach { it.pause() }
//                }
            }

            override fun onPlaybackStateChanged(state: Int) {

            }
        })

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                tvAnnotationListener.build(
                    BuildPoint(
                        player.currentPosition(),
                        player.currentAbsoluteTime(),
                        player,
                        isPlaying = true
                    )
                )
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

    fun feed(actionsList: List<Action>) {
        tvAnnotationListener.setActions(actionsList)
    }
}