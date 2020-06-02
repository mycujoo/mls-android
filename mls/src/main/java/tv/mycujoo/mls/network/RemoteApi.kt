package tv.mycujoo.mls.network

import tv.mycujoo.mls.entity.AnnotationSourceData
import tv.mycujoo.mls.entity.HighlightAction
import tv.mycujoo.mls.entity.TimeLineAction
import tv.mycujoo.mls.entity.TimeLineItem
import tv.mycujoo.mls.entity.actions.*
import tv.mycujoo.mls.model.PlacardSpecs

class RemoteApi : Api {

    override fun getActions(): List<ActionWrapper> {
        val listOfActionWrapper = ArrayList<ActionWrapper>()

        val actionRootSourceData = ActionRootSourceData()
        actionRootSourceData.id = "id_1000"
        actionRootSourceData.time = 10000L


        val sampleShowAnnouncementOverlayAction = getSampleShowAnnouncementOverlayAction()
        val sampleShowScoreboardAction = getSampleShowScoreboardAction()

        val sampleCommandAction = getSampleCommandAction()

        actionRootSourceData.actionsList.add(sampleShowAnnouncementOverlayAction)
        actionRootSourceData.actionsList.add(sampleShowScoreboardAction)
        actionRootSourceData.actionsList.add(sampleCommandAction)

        actionRootSourceData.build()

        actionRootSourceData.actionsList.forEach { abstractAction: AbstractAction ->
            val actionWrapper = ActionWrapper()
            actionWrapper.action = abstractAction
            actionWrapper.offset = 10000L

            listOfActionWrapper.add(actionWrapper)
        }

        return listOfActionWrapper
    }

    private fun getSampleShowAnnouncementOverlayAction(): ShowAnnouncementOverlayAction {
        val showAnnouncementOverlayAction = ShowAnnouncementOverlayAction()
        showAnnouncementOverlayAction.color = "#cccccc"
        showAnnouncementOverlayAction.line1 = "Line 1"
        showAnnouncementOverlayAction.line2 = " Line 2"
        showAnnouncementOverlayAction.imageUrl = "some url"

        showAnnouncementOverlayAction.viewId = "action_view_id_10000"

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

        showScoreboardOverlayAction.viewId = "action_view_id_10001"

        return showScoreboardOverlayAction
    }

    private fun getSampleCommandAction(): CommandAction {
        val commandAction = CommandAction()
        commandAction.verb = "hide"
        commandAction.targetViewId = "action_view_id_10001"
        commandAction.offset = 3000L

        return commandAction
    }


    override fun getPlacardsSpecs(): List<PlacardSpecs> {
        val placard = PlacardSpecs("LIVE_MODE_01", 2000L)

        return listOf(placard)
    }

    override fun getAnnotations(): List<AnnotationSourceData> {
        return emptyList()
        // do not delete! these are sample data to play with
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