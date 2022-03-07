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
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.jetbrains.annotations.Async
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.api.PlayerViewContract
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.model.ScreenTimerDirection
import tv.mycujoo.mcls.model.ScreenTimerFormat
import tv.mycujoo.mcls.network.MlsApi
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.thread

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
class PullingActionsInLiveEvent : E2ETest() {

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    @Inject
    lateinit var viewHandler: IViewHandler

    val videoIdlingResource = CountingIdlingResource("VIDEO")
    val helper = IdlingResourceHelper(videoIdlingResource)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(videoIdlingResource)
    }

    @Test
    fun testPullingActionsChange() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        val response = MockResponse()
            .setBody(eventResponse)

        mockWebServer.enqueue(response)

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo("e")

            // To Evade ExoPlayer OnMediaTransition Clearing Actions, We Wait a couple seconds
            Handler(Looper.getMainLooper()).postDelayed({
                mMLS.getVideoPlayer().setLocalAnnotations(actionSet1())
            }, 2000)

        }
        Thread.sleep(4000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 1

        UiThreadStatement.runOnUiThread {
            Handler(Looper.getMainLooper()).postDelayed({
                mMLS.getVideoPlayer().setLocalAnnotations(actionSet2())
            }, 6000)
        }
        Thread.sleep(10000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0
    }

    @Test
    fun testHideActionsWithoutShowAction() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        val response = MockResponse()
            .setBody(eventResponse)

        mockWebServer.enqueue(response)

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo("e")

            // To Evade ExoPlayer OnMediaTransition Clearing Actions, We Wait a couple seconds
            Handler(Looper.getMainLooper()).postDelayed({
                mMLS.getVideoPlayer().setLocalAnnotations(actionSet3())
            }, 2000)

        }
        Thread.sleep(15000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0
    }

    companion object {
        val mockWebServer = MockWebServer()

        val eventResponse = """
{
    "id": "e",
    "title": "New Event",
    "description": "",
    "thumbnail_url": "",
    "location": {
        "physical": {
            "venue": "",
            "city": "Amsterdam",
            "country_code": "NL",
            "continent_code": "EU",
            "coordinates": {
                "latitude": 52.3675734,
                "longitude": 4.9041389
            }
        }
    },
    "organiser": "",
    "start_time": "2021-12-07T16:00:34Z",
    "timezone": "Europe/Amsterdam",
    "status": "EVENT_STATUS_STARTED",
    "streams": [
        {
            "id": "ckwwahs2o000k0123hr2pqqwe",
            "full_url": "https://europe-west-hls.mls.mycujoo.tv/mohammadalkalaleeb/ckwwahs2o000k0123hr2pqqwe/master.m3u8",
            "has_secure_link": false,
            "is_protected": false,
            "has_geoblocking_rules": false,
            "dvr_window_size": "14400000",
            "drm": null,
            "error": null
        }
    ],
    "timeline_ids": [],
    "is_test": false,
    "is_protected": false,
    "package_ids": [],
    "metadata": {},
    "poster_url": "",
    "views": "0"
}
        """.trimIndent()

        private const val INVALID_TIME = -1L
        private const val TIMER_NAME = "main_timer"
        private const val DEFAULT_COLOR = "#FFFFFF"
        private const val SCOREBOARD_SVG_URL =
            "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"

        fun actionSet1(): List<Action> {
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
                Action.ShowOverlayAction(
                    id = "scoreboard_1",
                    offset = 1000L,
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

        fun actionSet2(): List<Action> {
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
                Action.ShowOverlayAction(
                    id = "100",
                    customId = "scoreboard",
                    offset = 1000L,
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
                )
            )

            actions.add(
                Action.HideOverlayAction(
                    id = "101",
                    offset = 6000L,
                    absoluteTime = INVALID_TIME,
                    customId = "scoreboard"
                )
            )

            return actions
        }

        fun actionSet3(): List<Action> {
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
                Action.HideOverlayAction(
                    id = "101",
                    offset = 6000L,
                    absoluteTime = INVALID_TIME,
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
                    name = "\$$TIMER_NAME",
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

    @Module
    @InstallIn(SingletonComponent::class)
    class TestNetworkModule {

        @Singleton
        @Provides
        fun provideRetrofit(): Retrofit {
            return Retrofit
                .Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl(mockWebServer.url("/"))
                .build()
        }

        @Singleton
        @Provides
        fun provideMlsApi(retrofit: Retrofit): MlsApi {
            return retrofit.create(MlsApi::class.java)
        }

        @Singleton
        @Provides
        fun provideOkHttp(): OkHttpClient {
            return OkHttpClient()
        }
    }
}
