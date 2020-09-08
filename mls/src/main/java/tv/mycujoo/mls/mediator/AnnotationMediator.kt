package tv.mycujoo.mls.mediator

import android.os.Handler
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.DISCONTINUITY_REASON_SEEK
import com.google.android.exoplayer2.Player.STATE_READY
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.mls.core.IAnnotationFactory
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.widgets.MLSPlayerView
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class AnnotationMediator(
    private var playerView: MLSPlayerView,
    private val annotationFactory: IAnnotationFactory,
    player: IPlayer,
    private val scheduler: ScheduledExecutorService,
    handler: Handler
) : IAnnotationMediator {

    /**region Fields*/
    private lateinit var eventListener: Player.EventListener
    private var hasPendingSeek: Boolean = false
    /**endregion */

    /**region Initialization*/
    init {
        initEventListener(player)

        feed()


        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                val currentPosition = player.currentPosition()

                annotationFactory.build(currentPosition, isPlaying = player.isPlaying(), interrupted = false)

                playerView.updateTime(currentPosition, player.duration())
            }
        }

        val scheduledRunnable = Runnable {
            handler.post(exoRunnable)
        }
        scheduler.scheduleAtFixedRate(scheduledRunnable, 1000L, 1000L, TimeUnit.MILLISECONDS)

    }

    private fun feed() {
        annotationFactory.setAnnotations(GetActionsFromJSONUseCase.result())
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
                        player.currentPosition(),
                        isPlaying = player.isPlaying(),
                        interrupted = true
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
        annotationFactory.build(player.currentPosition(), isPlaying = player.isPlaying(), interrupted = false)
    }
    /**endregion */
}