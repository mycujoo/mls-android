package tv.mycujoo.mls.tv.player

import android.os.Handler
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.mls.core.AnnotationFactory
import tv.mycujoo.mls.core.BuildPoint
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
    overlayContainer: ConstraintLayout,
    scheduler: ScheduledExecutorService,
    handler: Handler,
    coroutineScope: CoroutineScope,
    downloaderClient: DownloaderClient
) {

    private var annotationFactory: AnnotationFactory
    private var tvAnnotationListener: TvAnnotationListener
    private val viewHandler:
            ViewHandler =
        ViewHandler(coroutineScope, CountingIdlingResource("ViewIdentifierManager"))

    private var hasPendingSeek: Boolean = false

    init {
        viewHandler.setOverlayHost(overlayContainer)

        val overlayViewHelper =
            OverlayViewHelper(viewHandler, OverlayFactory(), AnimationFactory())

        tvAnnotationListener =
            TvAnnotationListener(
                overlayContainer,
                overlayViewHelper,
                downloaderClient
            )


        annotationFactory =
            AnnotationFactory(tvAnnotationListener, viewHandler.getVariableKeeper())

        player.addListener(object : Player.EventListener {
            override fun onPositionDiscontinuity(reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    hasPendingSeek = true
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && hasPendingSeek) {
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
                annotationFactory.build(
                    BuildPoint(
                        player.currentPosition(),
                        player.currentAbsoluteTime(),
                        player,
                        isPlaying = true,
                        isInterrupted = false
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

    fun feed(actionObjectList: List<ActionObject>) {
        annotationFactory.setAnnotations(actionObjectList)
    }
}