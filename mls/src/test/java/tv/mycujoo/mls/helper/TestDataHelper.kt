package tv.mycujoo.mls.helper

import tv.mycujoo.mls.model.AnnotationBundle
import tv.mycujoo.mls.model.AnnotationDataSource
import tv.mycujoo.mls.model.AnnotationType
import tv.mycujoo.mls.model.OverlayData

class TestDataHelper {

    companion object {
        fun getAnnotationBundle(): AnnotationBundle {
            return AnnotationBundle(AnnotationType.SHOW_OVERLAY, OverlayData("Primary text"))
        }

        fun getOverlayData(): OverlayData {
            return OverlayData("Primary text")
        }

        fun getAnnotationDataSource(): AnnotationDataSource {
            return AnnotationDataSource(
                AnnotationType.SHOW_OVERLAY,
                OverlayData("Primary text"),
                0L
            )
        }
    }
}