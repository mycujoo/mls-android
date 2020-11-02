package tv.mycujoo.mls.tv.player

import android.os.Handler
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.helper.DownloaderClient
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TvAnnotationMediator(
    player: ExoPlayer,
    tvOverlayContainer: TvOverlayContainer,
    private val scheduler: ScheduledExecutorService,
    private val dataManager: IDataManager,
    handler: Handler,
    private val dispatcher: CoroutineScope,
    downloaderClient: DownloaderClient
) {

    var tvAnnotationFactory: TvAnnotationFactory
    var tvAnnotationListener: TvAnnotationListener

    init {

        tvAnnotationListener = TvAnnotationListener(tvOverlayContainer, downloaderClient)
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

    fun feed(actionResponse: ActionResponse) {
        tvAnnotationFactory.setAnnotations(actionResponse)
    }
}