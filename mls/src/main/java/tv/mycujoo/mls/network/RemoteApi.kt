package tv.mycujoo.mls.network

import tv.mycujoo.mls.model.AnnotationDataSource
import tv.mycujoo.mls.model.AnnotationType
import tv.mycujoo.mls.model.OverlayData

class RemoteApi : Api {
    override fun getAnnotations(): List<AnnotationDataSource> {
        return listOf(
            AnnotationDataSource(AnnotationType.SHOW_OVERLAY, OverlayData("overlay_0"), 6000L),
            AnnotationDataSource(AnnotationType.SHOW_OVERLAY, OverlayData("overlay_1"), 12000L),
            AnnotationDataSource(AnnotationType.SHOW_OVERLAY, OverlayData("overlay_2"), 18000L)
        )
    }

    override fun getTimeLineMarkers(): LongArray {
        val longArray = LongArray(6)
        longArray[0] = 1000L
        longArray[1] = 20000L
        longArray[2] = 60000L
        longArray[3] = 320000L
        longArray[4] = 920000L
        longArray[5] = 1920000L

        return longArray
    }
}