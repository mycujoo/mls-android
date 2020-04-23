package tv.mycujoo.mls.cordinator

import android.os.Handler
import com.google.android.exoplayer2.SimpleExoPlayer
import tv.mycujoo.mls.core.AnnotationBuilderImpl
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.AnnotationPublisher
import tv.mycujoo.mls.model.AnnotationBundle
import tv.mycujoo.mls.network.Api
import tv.mycujoo.mls.widgets.PlayerWidget

class Coordinator(
    private val api: Api,
    private val publisher: AnnotationPublisher
) {

    internal lateinit var widget: PlayerWidget

    fun initialize(exoPlayer: SimpleExoPlayer, handler: Handler) {
        val listener = object : AnnotationListener {
            override fun onNewAnnotationAvailable(annotationBundle: AnnotationBundle) {
                widget.displayAnnotation(annotationBundle)
            }
        }
        publisher.setAnnotationListener(listener)


        val annotationBuilder = AnnotationBuilderImpl(publisher)
        annotationBuilder.buildPendings()

        annotationBuilder.addPendingAnnotations(api.getAnnotations())

        val runnable = object : Runnable {
            override fun run() {
                annotationBuilder.setCurrentTime(exoPlayer.currentPosition)
                annotationBuilder.buildPendings()
                handler.postDelayed(this, 1000L)
            }
        }

        handler.postDelayed(runnable, 1000L)
    }
}