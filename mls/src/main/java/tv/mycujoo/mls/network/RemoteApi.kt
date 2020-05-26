package tv.mycujoo.mls.network

import com.google.gson.Gson
import tv.mycujoo.mls.entity.*
import tv.mycujoo.mls.entity.actions.ActionRootSourceData
import tv.mycujoo.mls.entity.actions.ActionWrapper
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction
import tv.mycujoo.mls.model.MetaDataHolder
import tv.mycujoo.mls.model.Placard
import tv.mycujoo.mls.model.PlacardSpecs

class RemoteApi : Api {

    override fun getActions(): List<ActionWrapper> {
        val listOfActionWrapper = ArrayList<ActionWrapper>()

        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"
        actionRootSourceData.time = 10000L


        val sampleShowAnnouncementOverlayAction = getSampleShowAnnouncementOverlayAction()
        val sampleShowScoreboardAction = getSampleShowScoreboardAction()

        actionRootSourceData.actionsList.add(sampleShowAnnouncementOverlayAction)
        actionRootSourceData.actionsList.add(sampleShowScoreboardAction)

        val actionWrapper = ActionWrapper()
        actionWrapper.action = sampleShowAnnouncementOverlayAction
        actionWrapper.offset = actionRootSourceData.time ?: -1

        listOfActionWrapper.add(actionWrapper)

        return listOfActionWrapper
    }

    private fun getSampleShowAnnouncementOverlayAction(): ShowAnnouncementOverlayAction {
        val showAnnouncementOverlayAction = ShowAnnouncementOverlayAction()
        showAnnouncementOverlayAction.color = "#cccccc"
        showAnnouncementOverlayAction.line1 = "Line 1"
        showAnnouncementOverlayAction.line2 = " Line 2"
        showAnnouncementOverlayAction.imageUrl = "some url"

        return showAnnouncementOverlayAction
    }

    private fun getSampleShowScoreboardAction(): ShowScoreboardOverlayAction {
        val showScoreboardOverlayAction = ShowScoreboardOverlayAction()
        showScoreboardOverlayAction.colorLeft = "#cccccc"
        showScoreboardOverlayAction.colorRight = "#f4f4f4"
        showScoreboardOverlayAction.abbrLeft = "FCB"
        showScoreboardOverlayAction.abbrRight = " CFC"
        showScoreboardOverlayAction.scoreLeft = "0"
        showScoreboardOverlayAction.scoreRight = "0"



        return showScoreboardOverlayAction
    }


    override fun getPlacardsSpecs(): List<PlacardSpecs> {
        val listOfActions = listOf<String>("SHOW_SCORE_BOARD", "HOME_TEAM_PLUS_1")

        val goalMetaData = MetaDataHolder("tag", "Goal")
        val listOfMetaData = listOf(goalMetaData)
        val gson = Gson()
        val metaDataJson = gson.toJson(listOfActions)

        val placardType = Placard(9000, "homeGoal", metaDataJson, listOfActions)

        val placard = PlacardSpecs("LIVE_MODE_01", 2000L)


        return listOf(placard)
    }

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

    override fun getTimeLineMarkers(): List<TimeLineItem> {

        val mutableList = ArrayList<TimeLineItem>(6)

        mutableList.add(TimeLineItem(1000L, TimeLineAction(3000, "#ff00ff", "Goal")))
        mutableList.add(TimeLineItem(60000L, TimeLineAction(3000, "#ff00ff", "Goal")))
        mutableList.add(TimeLineItem(320000L, TimeLineAction(3000, "#ff00ff", "Foul")))
        mutableList.add(TimeLineItem(920000L, TimeLineAction(3000, "#ff00ff", "Goal")))
        mutableList.add(TimeLineItem(1920000L, TimeLineAction(3000, "#ff00ff", "Goal")))
        mutableList.add(TimeLineItem(2920000L, TimeLineAction(3000, "#ff00ff", "Free Kick")))

        return mutableList
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