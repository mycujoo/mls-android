package tv.mycujoo.mls.mediator

import android.os.Handler
import android.os.Looper
import tv.mycujoo.mls.core.AnnotationFactory
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.helper.DownloaderClient
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.widgets.MLSPlayerView
import java.util.concurrent.Executors

class AnnotationMediatorFactory {
    companion object {
        fun createAnnotationMediator(
            MLSPlayerView: MLSPlayerView,
            internalBuilder: InternalBuilder,
            player: IPlayer
        ): AnnotationMediator {
            val annotationListener =
                AnnotationListener(
                    MLSPlayerView,
                    internalBuilder.overlayViewHelper,
                    DownloaderClient(internalBuilder.okHttpClient)
                )

            val annotationFactory = AnnotationFactory(
                annotationListener,
                internalBuilder.variableKeeper
            )

            val annotationMediator = AnnotationMediator(
                MLSPlayerView,
                annotationFactory,
                internalBuilder.dataManager,
                internalBuilder.dispatcher,
                player,
                Executors.newScheduledThreadPool(1),
                Handler(Looper.getMainLooper()),
                internalBuilder.logger
            )

            annotationMediator.initPlayerView(MLSPlayerView)

            return annotationMediator
        }
    }
}