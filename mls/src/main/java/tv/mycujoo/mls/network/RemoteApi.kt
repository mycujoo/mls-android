package tv.mycujoo.mls.network

import tv.mycujoo.mls.model.AnnotationDataSource
import tv.mycujoo.mls.model.AnnotationType
import tv.mycujoo.mls.model.HighlightDataSource
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

    override fun getHighlights(): List<AnnotationDataSource> {
        return listOf(
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                99000L,
                HighlightDataSource(
                    "Goal 1",
                    "2'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                360000L,
                HighlightDataSource(
                    "Goal 2",
                    "6'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                880000L,
                HighlightDataSource(
                    "Goal 3",
                    "14'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                980000L,
                HighlightDataSource(
                    "Goal 4",
                    "16'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                1080000L,
                HighlightDataSource(
                    "Goal 5",
                    "18'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                1180000L,
                HighlightDataSource(
                    "Goal 6",
                    "19'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                1280000L,
                HighlightDataSource(
                    "Goal 7",
                    "21'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationDataSource(
                AnnotationType.HIGHLIGHT,
                null,
                1380000L,
                HighlightDataSource(
                    "Goal 7",
                    "23'",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            )
        )
    }
}