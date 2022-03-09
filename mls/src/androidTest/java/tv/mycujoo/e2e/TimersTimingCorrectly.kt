package tv.mycujoo.e2e

import android.os.Handler
import android.os.Looper
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.hilt.android.testing.HiltAndroidTest
import org.joda.time.DateTime
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat

@HiltAndroidTest
class TimersTimingCorrectly : E2ETest() {

    private val videoIdlingResource = CountingIdlingResource("VIDEO")

    val helper = IdlingResourceHelper(videoIdlingResource)

    @Test
    fun testTimerAdjustment() {
        val event = EventEntity(
            id = "ckw25ntnkxlam0hbqnfhx3gk0",
            title = "Top 10 Gol Serie C 2021/22 - 14^ Giornata",
            description = "I dieci gol più belli della 14^ giornata di Serie C",
            thumbnailUrl = null,
            poster_url = null,
            location = null,
            organiser = null,
            start_time = DateTime.parse("2021-11-14T14:30:00.000+01:00"),
            status = EventStatus.EVENT_STATUS_FINISHED,
            streams = listOf(
                Stream(
                    id = "ckw25ntnkxlam0hbqnfhx3gk0",
                    dvrWindowString = null,
                    fullUrl = "https://europe-west-hls.mls.mycujoo.tv/mohammadalkalaleeb/ckwwahs2o000k0123hr2pqqwe/master.m3u8",
                    widevine = null,
                    errorCodeAndMessage = null
                )
            ),
            timezone = null,
            timeline_ids = listOf(),
            metadata = null,
            is_test = false,
            isNativeMLS = false
        )

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(event)

            // To Evade ExoPlayer OnMediaTransition Clearing Actions, We Wait a couple seconds
            Handler(Looper.getMainLooper()).postDelayed({
                val actions = actionSet1()
//                actions.add(
//                    Action.AdjustTimerAction(
//                        id = "timer",
//                        offset = 0,
//                        absoluteTime = INVALID_TIME,
//                        name = "\$main_timer",
//                        value = 50000
//                    )
//                )

                mMLS.getVideoPlayer().setLocalAnnotations(actions)
            }, 2000)
        }

        Thread.sleep(25000)
    }

    companion object {
        private const val INVALID_TIME = -1L
        private const val TIMER_NAME = "main_timer"
        private const val DEFAULT_COLOR = "#FFFFFF"
        private const val SCOREBOARD_SVG_URL =
            "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"

        fun actionSet1(): MutableList<Action> {
            val actions = mutableListOf<Action>()

            actions.addAll(footballDefaultActions())

            actions.add(
                Action.StartTimerAction(
                    id = "timer",
                    offset = 0,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer"
                )
            )

            actions.add(
                Action.PauseTimerAction(
                    id = "timer",
                    offset = 250,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer"
                )
            )

            actions.add(
                Action.StartTimerAction(
                    id = "timer",
                    offset = 500,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer",
                )
            )

            actions.add(
                Action.PauseTimerAction(
                    id = "timer",
                    offset = 750,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer"
                )
            )

            actions.add(
                Action.StartTimerAction(
                    id = "timer",
                    offset = 1000,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer"
                )
            )

            actions.add(
                Action.PauseTimerAction(
                    id = "timer",
                    offset = 1250,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer"
                )
            )

            actions.add(
                Action.AdjustTimerAction(
                    id = "timer",
                    offset = 1450,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer",
                    value = 50000
                )
            )

            actions.add(
                Action.StartTimerAction(
                    id = "timer",
                    offset = 1500,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer",
                )
            )

            actions.add(
                Action.PauseTimerAction(
                    id = "timer",
                    offset = 1750,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer"
                )
            )

            actions.add(
                Action.StartTimerAction(
                    id = "timer",
                    offset = 2000,
                    absoluteTime = INVALID_TIME,
                    name = "\$main_timer",
                )
            )

//            actions.add(
//                Action.PauseTimerAction(
//                    id = "timer",
//                    offset = 2250,
//                    absoluteTime = INVALID_TIME,
//                    name = "\$main_timer"
//                )
//            )

            actions.add(
                Action.ShowOverlayAction(
                    id = "scoreboard",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    svgData = SvgData(SCOREBOARD_SVG_URL),
                    viewSpec = ViewSpec(
                        PositionGuide(left = 5F, top = 5F),
                        Pair(35F, 100F)
                    ),
                    placeHolders = listOf(
                        "\$home_score",
                        "\$away_score",
                        "\$main_timer",
                        "\$home_abbr",
                        "\$away_abbr",
                        "\$home_color",
                        "\$away_color"
                    ),
                    customId = "scoreboard"
                )
            )

            return actions
        }

        private fun footballDefaultActions(
            homeTeamAbbr: String = "HOME",
            awayTeamAbbr: String = "AWAY",
            homeTeamColor: String = DEFAULT_COLOR,
            awayTeamColor: String = DEFAULT_COLOR
        ): Collection<Action> {
            return arrayListOf(
                // main timer
                Action.CreateTimerAction(
                    id = "1",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    name = "\$${TIMER_NAME}",
                    format = ScreenTimerFormat.MINUTES_SECONDS,
                    direction = ScreenTimerDirection.UP,
                    startValue = 0L,
                    capValue = 999 * 60 * 1000L
                ),

                // home team score
                Action.CreateVariableAction(
                    id = "2",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    variable = Variable.LongVariable("\$home_score", 0L)
                ),

                // away team score
                Action.CreateVariableAction(
                    id = "3",
                    offset = 0L,
                    absoluteTime = -INVALID_TIME,
                    variable = Variable.LongVariable("\$away_score", 0L)
                ),

                // home team abbreviation
                Action.CreateVariableAction(
                    id = "4",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    variable = Variable.StringVariable("\$home_abbr", homeTeamAbbr)
                ),

                // away team abbreviation
                Action.CreateVariableAction(
                    id = "5",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    variable = Variable.StringVariable("\$away_abbr", awayTeamAbbr)
                ),

                // home team color
                Action.CreateVariableAction(
                    id = "6",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    variable = Variable.StringVariable("\$home_color", homeTeamColor)
                ),

                // away team color
                Action.CreateVariableAction(
                    id = "7",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    variable = Variable.StringVariable("\$away_color", awayTeamColor)
                ),

                // announcement_line1, used for Goal overlay
                Action.CreateVariableAction(
                    id = "8",
                    offset = 0L,
                    absoluteTime = INVALID_TIME,
                    variable = Variable.StringVariable("\$announcement_line1", "Goal")
                )
            )
        }
    }
}