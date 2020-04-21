package tv.mycujoo.mls.core

import tv.mycujoo.mls.model.AnnotationBundle

interface AnnotationListener {
    fun onNewAnnotationAvailable(annotationBundle: AnnotationBundle)

}
