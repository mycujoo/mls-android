package tv.mycujoo

import android.content.UriPermission
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.helper.sampleSvgString

class TestData {
    companion object {
        fun getSampleShowOverlayAction(
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(PositionGuide(left = 10F, top = 10F), Pair(30F, 0F))
            val introTransitionSpec = TransitionSpec(C.ONE_SECOND_IN_MS, AnimationType.NONE, 0L)
            val outroTransitionSpec = TransitionSpec(2000L, AnimationType.NONE, 0L)
            val svgData = SvgData(null, sampleSvgString)

            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = introTransitionSpec.offset,
                absoluteTime = -1L,
                svgData = svgData,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
        }

        fun getSampleShowOverlayAction(
            introTransitionSpec: TransitionSpec,
            outroOffset: Long
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            val outroTransitionSpec = TransitionSpec(outroOffset, AnimationType.NONE, 0L)
            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = introTransitionSpec.offset,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
        }

        fun getSampleShowOverlayAction(
            introTransitionSpec: TransitionSpec,
            outroTransitionSpec: TransitionSpec
        ): Action.ShowOverlayAction {
            val viewSpec = ViewSpec(null, null)
            return Action.ShowOverlayAction(
                id = "id_1001",
                offset = introTransitionSpec.offset,
                absoluteTime = -1L,
                svgData = null,
                duration = 0L,
                viewSpec = viewSpec,
                introTransitionSpec = introTransitionSpec,
                outroTransitionSpec = outroTransitionSpec,
                placeHolders = emptyList()
            )
        }

        fun getSampleScoreboardActionsList(): MutableList<Action> {
            val actionsList = mutableListOf<Action>()

            actionsList.add(
                Action.StartTimerAction(
                    id = "timer",
                    absoluteTime = 0,
                    offset = 0,
                    name = "\$main_timer"
                )
            )

            // home team score
            actionsList.add(
                Action.CreateVariableAction(
                    id = "2",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.LongVariable("\$home_score", 0L)
                )
            )

            // away team score
            actionsList.add(
                Action.CreateVariableAction(
                    id = "3",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.LongVariable("\$away_score", 0L)
                )
            )

            // home team abbreviation
            actionsList.add(
                Action.CreateVariableAction(
                    id = "4",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.StringVariable("\$home_abbr", "HOME")
                )
            )

            // away team abbreviation
            actionsList.add(
                Action.CreateVariableAction(
                    id = "5",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.StringVariable("\$away_abbr", "AWAY")
                )
            )

            // home team color
            actionsList.add(
                Action.CreateVariableAction(
                    id = "6",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.StringVariable("\$home_color", "#FFFFFF")
                )
            )

            // away team color
            actionsList.add(
                Action.CreateVariableAction(
                    id = "7",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.StringVariable("\$away_color", "#FFFFFF")
                )
            )

            // announcement_line1, used for Goal overlay
            actionsList.add(
                Action.CreateVariableAction(
                    id = "8",
                    offset = 0L,
                    absoluteTime = UriPermission.INVALID_TIME,
                    variable = Variable.StringVariable("\$announcement_line1", "Goal")
                )
            )

            return actionsList
        }
    }
}