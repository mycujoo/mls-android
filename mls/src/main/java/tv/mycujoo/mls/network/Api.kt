package tv.mycujoo.mls.network

import tv.mycujoo.mls.model.AnnotationDataSource

interface Api {

    fun getAnnotations(): List<AnnotationDataSource>

}
