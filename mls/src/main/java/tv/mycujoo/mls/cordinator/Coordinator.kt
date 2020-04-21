package tv.mycujoo.mls.cordinator

import tv.mycujoo.mls.model.AnnotationBundle
import tv.mycujoo.mls.core.AnnotationListener
import tv.mycujoo.mls.core.AnnotationPublisher
import tv.mycujoo.mls.widgets.PlayerWidget

class Coordinator(
    private val widget: PlayerWidget,
    private val publisher: AnnotationPublisher
) {

    init {
        val listener = object : AnnotationListener {
            override fun onNewAnnotationAvailable(annotationBundle: AnnotationBundle) {
                widget.displayAnnotation(annotationBundle)
            }
        }
        publisher.setAnnotationListener(listener)
    }
}