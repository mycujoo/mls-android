package tv.mycujoo.mls.tv.player

import android.os.Handler
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.domain.entity.ActionObject
import tv.mycujoo.mls.helper.DownloaderClient
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TvAnnotationMediator(
    player: ExoPlayer,
    tvOverlayContainer: TvOverlayContainer,
    scheduler: ScheduledExecutorService,
    handler: Handler,
    coroutineScope: CoroutineScope,
    downloaderClient: DownloaderClient
) {

    private var tvAnnotationFactory: TvAnnotationFactory
    private var tvAnnotationListener: TvAnnotationListener =
        TvAnnotationListener(
            tvOverlayContainer,
            coroutineScope,
            CountingIdlingResource("ViewIdentifierManager"),
            downloaderClient
        )

    init {

        tvAnnotationFactory = TvAnnotationFactory(tvAnnotationListener)

        val exoRunnable = Runnable {
            if (player.isPlaying) {
                val currentPosition = player.currentPosition

                tvAnnotationFactory.build(
                    currentPosition,
                    isPlaying = player.isPlaying,
                    interrupted = false
                )
            }
        }

        val scheduledRunnable = Runnable {
            handler.post(exoRunnable)
        }

        scheduler.scheduleAtFixedRate(scheduledRunnable, 1000L, 1000L, TimeUnit.MILLISECONDS)
    }

    fun feed(actionObjectList: List<ActionObject>) {
        tvAnnotationFactory.setAnnotations(actionObjectList)
    }
}