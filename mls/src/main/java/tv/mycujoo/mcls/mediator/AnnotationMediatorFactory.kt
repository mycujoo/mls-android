package tv.mycujoo.mcls.mediator

import android.os.Handler
import android.os.Looper
import tv.mycujoo.mcls.core.AnnotationFactory
import tv.mycujoo.mcls.core.AnnotationListener
import tv.mycujoo.mcls.core.InternalBuilder
import tv.mycujoo.mcls.helper.DownloaderClient
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.widgets.MLSPlayerView
import java.util.concurrent.Executors

//class AnnotationMediatorFactory {
//    companion object {
////        fun createAnnotationMediator(
////            MLSPlayerView: MLSPlayerView,
////            internalBuilder: InternalBuilder,
////            player: IPlayer
////        ): AnnotationMediator {
////            val annotationListener = AnnotationListener(
////                MLSPlayerView,
////                internalBuilder.overlayViewHelper,
////                DownloaderClient(internalBuilder.okHttpClient)
////            )
////
////            val annotationFactory = AnnotationFactory(
////                annotationListener,
////                internalBuilder.variableKeeper
////            )
////
////            val annotationMediator = AnnotationMediator(
////                annotationFactory,
////                internalBuilder.dataManager,
////                internalBuilder.dispatcher,
////                player,
////                Executors.newScheduledThreadPool(1),
////                Handler(Looper.getMainLooper()),
////                internalBuilder.logger
////            )
////
////            annotationMediator.initPlayerView(MLSPlayerView)
////
////            return annotationMediator
////        }
//    }
//}