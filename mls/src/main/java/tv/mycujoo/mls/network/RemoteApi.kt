package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.*

class RemoteApi : Api {

    override fun getAnnotations(): List<AnnotationSourceData> {
        return listOf(
            AnnotationSourceData(
                4000L,
                getOverLayAction(LayoutType.BASIC_SINGLE_LINE, "First text", "Second text")
            ),
            AnnotationSourceData(
                8000L,
                getOverLayAction(LayoutType.BASIC_SINGLE_LINE, "First text_2", "Second text_2")
            ),
            AnnotationSourceData(
                12000L, OverLayAction(
                    103,
                    3000L,
                    LayoutType.BASIC_DOUBLE_LINE,
                    LayoutPosition.BOTTOM_LEFT,
                    false,
                    "First text_3",
                    "Second text_3",
                    "http://icons.iconarchive.com/icons/giannis-zographos/spanish-football-club/72/FC-Barcelona-icon.png",
                    "secondLine 0", "secondLine 1"
                )
            ),
            AnnotationSourceData(
                16000L, OverLayAction(
                    104,
                    3000L,
                    LayoutType.BASIC_DOUBLE_LINE,
                    LayoutPosition.TOP_LEFT,
                    false,

                    "First text_3",
                    "Second text_3",
                    "http://icons.iconarchive.com/icons/giannis-zographos/spanish-football-club/72/FC-Barcelona-icon.png",
                    "secondLine 0", "secondLine 1"
                )
            ),
            AnnotationSourceData(
                18000L, OverLayAction(
                    105,
                    3000L,
                    LayoutType.BASIC_SCORE_BOARD,
                    LayoutPosition.TOP_LEFT,
                    false,
                    "FCB",
                    "CFC",
                    null,
                    "0", "1"
                )
            ),
            AnnotationSourceData(
                9000L, OverLayAction(
                    106,
                    3000L,
                    LayoutType.BASIC_SINGLE_LINE,
                    LayoutPosition.TOP_RIGHT,
                    true,
                    "FCB 0",
                    "CFC 0",
                    null,
                    null
                )
            ),
            AnnotationSourceData(
                14000L, OverLayAction(
                    106,
                    3000L,
                    LayoutType.BASIC_SINGLE_LINE,
                    LayoutPosition.BOTTOM_RIGHT,
                    false,
                    "FCB 1",
                    "CFC 1",
                    null,
                    null
                )
            ),
            AnnotationSourceData(
                5000L, HighlightAction(
                    136,
                    5000L,
                    "5'",
                    "FCB 1",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            ),
            AnnotationSourceData(
                5000L, HighlightAction(
                    13,
                    9000L,
                    "9'",
                    "FCB 2",
                    "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
                )
            )
        )
    }

    private fun getOverLayAction(
        type: LayoutType,
        firstText: String,
        secondText: String
    ): OverLayAction {
        return OverLayAction(
            101,
            3000L,
            LayoutType.BASIC_SINGLE_LINE,
            LayoutPosition.BOTTOM_LEFT,
            false,
            firstText,
            secondText,
            "http://icons.iconarchive.com/icons/giannis-zographos/spanish-football-club/72/FC-Barcelona-icon.png"
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

    override fun getHighlights(): List<HighlightAction> {
        return listOf(

            HighlightAction(
                125,
                15000L,
                "15'",
                "FCB 3",
                "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
            ),
            HighlightAction(
                126,
                2000L,
                "20'",
                "FCB 4",
                "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
            ),
            HighlightAction(
                127,
                25000L,
                "25'",
                "FCB 5",
                "https://playlists.mycujoo.football/eu/ck8u05tfu1u090hew2kgobnud/master.m3u8"
            )
        )

    }

}