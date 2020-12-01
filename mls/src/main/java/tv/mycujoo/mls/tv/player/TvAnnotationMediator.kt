package tv.mycujoo.mls.tv.player

import android.os.Handler
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.mls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mls.helper.AnimationFactory
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.helper.OverlayFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.player.IPlayer
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TvAnnotationMediator(
    player: IPlayer,
    tvOverlayContainer: TvOverlayContainer,
    scheduler: ScheduledExecutorService,
    handler: Handler,
    coroutineScope: CoroutineScope,
    downloaderClient: DownloaderClient
) {

    private var tvAnnotationFactory: TvAnnotationFactory
    private var tvAnnotationListener: TvAnnotationListener
    private val viewHandler:
            ViewHandler =
        ViewHandler(coroutineScope, CountingIdlingResource("ViewIdentifierManager"))

    private var hasPendingSeek: Boolean = false

    init {
        viewHandler.setOverlayHost(tvOverlayContainer)

        val overlayViewHelper =
            OverlayViewHelper(viewHandler, OverlayFactory(), AnimationFactory())

        tvAnnotationListener =
            TvAnnotationListener(
                tvOverlayContainer,
                overlayViewHelper,
                downloaderClient
            )


        tvAnnotationFactory =
            TvAnnotationFactory(tvAnnotationListener, viewHandler.getVariableKeeper())

        player.addListener(object : Player.EventListener {
            override fun onPositionDiscontinuity(reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && hasPendingSeek) {
                    hasPendingSeek = false

                    tvAnnotationFactory.build(
                        player.currentPosition(),
                        isPlaying = player.isPlaying(),
                        interrupted = true
                    )
                }

            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    viewHandler.getAnimations().forEach { it.resume() }
                } else {
                    viewHandler.getAnimations().forEach { it.pause() }
                }
            }

            override fun onPlaybackStateChanged(state: Int) {

            }
        })

        val exoRunnable = Runnable {
            if (player.isPlaying()) {
                val currentPosition = player.currentPosition()

                tvAnnotationFactory.build(
                    currentPosition,
                    isPlaying = true,
                    interrupted = false
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

    fun feed(actionObjectList: List<ActionObject>) {
        tvAnnotationFactory.setAnnotations(actionObjectList)
    }
}