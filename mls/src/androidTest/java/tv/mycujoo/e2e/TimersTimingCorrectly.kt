package tv.mycujoo.e2e

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(PlayerModule::class)
class TimersTimingCorrectly : E2ETest() {

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    @Inject
    lateinit var variableKeeper: VariableKeeper

    private val videoIdlingResource = CountingIdlingResource("VIDEO")
    private val warmupResource = CountingIdlingResource("WARM_UP")
    val helper = IdlingResourceHelper(videoIdlingResource)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
        IdlingRegistry.getInstance().register(warmupResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(videoIdlingResource)
        IdlingRegistry.getInstance().unregister(warmupResource)
    }

    @Test
    fun testTimerAdjustment() {
        videoIdlingResource.increment()
        warmupResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(sampleEvent)

            // To Evade ExoPlayer OnMediaTransition Clearing Actions, We Wait a couple seconds
            Handler(Looper.getMainLooper()).postDelayed({
                val actions = actionSet1()

                mMLS.getVideoPlayer().setLocalAnnotations(actions)
            }, 1000)
        }


        Handler(Looper.getMainLooper()).postDelayed({
            // TODO: Improve This, this doesn't match correct behavior
            variableKeeper.observeOnTimer("\$${TIMER_NAME}") {
                it.second shouldBeEqualTo "1:00"
                warmupResource.decrement()
            }
        }, 10000)

        // To make sure the test has passed
        Thread.sleep(11000)
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

            actions.addAll(
                listOf(
                    Action.StartTimerAction(
                        id = "timer",
                        offset = 1000,
                        absoluteTime = INVALID_TIME,
                        name = "\$main_timer"
                    ),
                    Action.PauseTimerAction(
                        id = "timer",
                        offset = 6000,
                        absoluteTime = INVALID_TIME,
                        name = "\$main_timer"
                    ),
                    // This is causing trouble if the timer is paused
                    Action.AdjustTimerAction(
                        id = "timer",
                        offset = 7000,
                        absoluteTime = INVALID_TIME,
                        name = "\$main_timer",
                        value = 60000
                    ),
                    Action.StartTimerAction(
                        id = "timer",
                        offset = 7500,
                        absoluteTime = INVALID_TIME,
                        name = "\$main_timer",
                    ),
                )
            )

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

        val sampleEvent = EventEntity(
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