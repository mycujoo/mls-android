package tv.mycujoo.mcls.tv.player

import android.os.Handler
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.Player
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.domain.entity.Action
import tv.mycujoo.mcls.core.AnnotationFactory
import tv.mycujoo.mcls.core.BuildPoint
import tv.mycujoo.mcls.enum.C.Companion.ONE_SECOND_IN_MS
import tv.mycujoo.mcls.helper.AnimationFactory
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.helper.OverlayFactory
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.player.IPlayer
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
        ViewHandler(CountingIdlingResource("ViewIdentifierManager"))
    private val variableTranslator = VariableTranslator(coroutineScope)
    private val variableKeeper = VariableKeeper(coroutineScope)

    private var hasPendingSeek: Boolean = false

    init {
        viewHandler.setOverlayHost(overlayContainer)

        val overlayViewHelper =
            OverlayViewHelper(
                viewHandler,
                OverlayFactory(),
                AnimationFactory(),
                variableTranslator,
                variableKeeper
            )

        tvAnnotationListener =
            TvAnnotationListener(
                overlayContainer,
                overlayViewHelper,
                downloaderClient
            )


        annotationFactory =
            AnnotationFactory(tvAnnotationListener, variableKeeper)

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
                            player.isPlaying()
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
        annotationFactory.setActions(actionsList)
    }
}