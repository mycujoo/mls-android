package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.entity.AnnotationSourceData

interface Api {

    fun getAnnotations(): List<AnnotationSourceData>
    fun getTimeLineMarkers(): LongArray

    fun getHighlights(): List<HighlightAction>

}
