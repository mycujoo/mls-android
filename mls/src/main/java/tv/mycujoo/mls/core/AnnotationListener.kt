package tv.mycujoo.mls.core

import tv.mycujoo.mls.entity.AnnotationSourceData

interface AnnotationListener {
    fun onNewAnnotationAvailable(annotationSourceData: AnnotationSourceData)

}
