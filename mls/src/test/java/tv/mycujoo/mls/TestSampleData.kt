package tv.mycujoo.mls

import tv.mycujoo.mls.entity.actions.CommandAction
import tv.mycujoo.mls.entity.actions.ShowAnnouncementOverlayAction
import tv.mycujoo.mls.entity.actions.ShowScoreboardOverlayAction

class TestSampleData {
    companion object {

        fun getSampleShowAnnouncementOverlayAction(): ShowAnnouncementOverlayAction {
            val showAnnouncementOverlayAction = ShowAnnouncementOverlayAction()
            showAnnouncementOverlayAction.color = "#cccccc"
            showAnnouncementOverlayAction.line1 = "Line 1"
            showAnnouncementOverlayAction.line2 = " Line 2"
            showAnnouncementOverlayAction.imageUrl = "some url"

            showAnnouncementOverlayAction.viewId = "action_view_id_10000"

            return showAnnouncementOverlayAction
        }

        fun getSampleShowScoreboardAction(): ShowScoreboardOverlayAction {
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

        fun getSampleShowScoreboardAction_WithDismissingParams(): ShowScoreboardOverlayAction {
            val showScoreboardOverlayAction = ShowScoreboardOverlayAction()
            showScoreboardOverlayAction.colorLeft = "#cccccc"
            showScoreboardOverlayAction.colorRight = "#f4f4f4"
            showScoreboardOverlayAction.abbrLeft = "FCB"
            showScoreboardOverlayAction.abbrRight = " CFC"
            showScoreboardOverlayAction.scoreLeft = "0"
            showScoreboardOverlayAction.scoreRight = "0"

            showScoreboardOverlayAction.viewId = "action_view_id_10002"
            showScoreboardOverlayAction.dismissible = true
            showScoreboardOverlayAction.dismissIn = 3000L

            return showScoreboardOverlayAction
        }

        fun getSampleCommandAction(verb: String): CommandAction {
            val commandAction = CommandAction()
            commandAction.verb = verb
            commandAction.targetViewId = "action_view_id_10001"
            commandAction.offset = 100L

            return commandAction
        }
    }

}
