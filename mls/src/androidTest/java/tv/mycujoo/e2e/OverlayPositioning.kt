package tv.mycujoo.e2e

import android.content.Context
import android.content.UriPermission.INVALID_TIME
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.amshove.kluent.shouldBeEqualTo
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.entity.ServerConstants
import tv.mycujoo.data.model.*
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
class OverlayPositioning : E2ETest() {

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
    fun testOverlayPositionTopLeft() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(testEvent)
        }

        val actionsList = getTestAnnotations()
        for (i in 0..5) {
            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 0,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(left = i * 20f, top = i * 20f),
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
                    customId = "$i",
                )
            )

            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 10000,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(left = i * 20f, top = i * 20f),
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
                    customId = "$i",
                )
            )

            actionsList.add(
                Action.HideOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 5000,
                    customId = "$i",
                )
            )
        }

        mMLS.getVideoPlayer().setLocalAnnotations(actionsList)

        helper.waitUntilIdle()

        Thread.sleep(5000)

        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo (position * 20f) / 100
        }

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(7000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo (position * 20f) / 100
        }
    }

    @Test
    fun testOverlayPositionTopRight() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(testEvent)
        }

        val actionsList = getTestAnnotations()
        for (i in 0..5) {
            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 0,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(right = i * 20f, top = i * 20f),
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
                    customId = "$i",
                )
            )

            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 10000,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(right = i * 20f, top = i * 20f),
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
                    customId = "$i",
                )
            )

            actionsList.add(
                Action.HideOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 5000,
                    customId = "$i",
                )
            )
        }

        mMLS.getVideoPlayer().setLocalAnnotations(actionsList)

        helper.waitUntilIdle()

        Thread.sleep(5000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(7000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }
    }

    @Test
    fun testOverlayPositionBottomLeft() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(testEvent)
        }

        val actionsList = getTestAnnotations()
        for (i in 0..5) {
            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 0,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(bottom = i * 20f, left = i * 20f),
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
                    customId = "$i",
                )
            )

            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 10000,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(bottom = i * 20f, left = i * 20f),
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
                    customId = "$i",
                )
            )

            actionsList.add(
                Action.HideOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 5000,
                    customId = "$i",
                )
            )
        }

        mMLS.getVideoPlayer().setLocalAnnotations(actionsList)

        helper.waitUntilIdle()

        Thread.sleep(5000)

        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo ((position * 20f) / 100)
        }

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(7000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo ((position * 20f) / 100)
        }
    }

    @Test
    fun testOverlayPositionBottomRight() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(testEvent)
        }

        val actionsList = getTestAnnotations()
        for (i in 0..5) {
            // Add the initial show actions (Offset 0)
            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 0,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(right = i * 20f, bottom = i * 20f),
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
                    customId = "$i",
                )
            )

            // Add the reshow show actions (Offset 10 sec)
            actionsList.add(
                Action.ShowOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 10000,
                    svgData = SvgData(
                        svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                    ),
                    viewSpec = ViewSpec(
                        PositionGuide(right = i * 20f, bottom = i * 20f),
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
                    customId = "$i",
                )
            )

            // Hide Actions in between the 2 show overlays (Offset 5 sec)
            actionsList.add(
                Action.HideOverlayAction(
                    id = "overlay_$i",
                    absoluteTime = 0,
                    offset = 5000,
                    customId = "$i",
                )
            )
        }

        mMLS.getVideoPlayer().setLocalAnnotations(actionsList)

        helper.waitUntilIdle()

        Thread.sleep(3000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(7000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }
    }

    private fun getTestAnnotations(): MutableList<Action> {
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
                absoluteTime = INVALID_TIME,
                variable = Variable.LongVariable("\$home_score", 0L)
            )
        )

        // away team score
        actionsList.add(
            Action.CreateVariableAction(
                id = "3",
                offset = 0L,
                absoluteTime = -INVALID_TIME,
                variable = Variable.LongVariable("\$away_score", 0L)
            )
        )

        // home team abbreviation
        actionsList.add(
            Action.CreateVariableAction(
                id = "4",
                offset = 0L,
                absoluteTime = INVALID_TIME,
                variable = Variable.StringVariable("\$home_abbr", "HOME")
            )
        )

        // away team abbreviation
        actionsList.add(
            Action.CreateVariableAction(
                id = "5",
                offset = 0L,
                absoluteTime = INVALID_TIME,
                variable = Variable.StringVariable("\$away_abbr", "AWAY")
            )
        )

        // home team color
        actionsList.add(
            Action.CreateVariableAction(
                id = "6",
                offset = 0L,
                absoluteTime = INVALID_TIME,
                variable = Variable.StringVariable("\$home_color", "#FFFFFF")
            )
        )

        // away team color
        actionsList.add(
            Action.CreateVariableAction(
                id = "7",
                offset = 0L,
                absoluteTime = INVALID_TIME,
                variable = Variable.StringVariable("\$away_color", "#FFFFFF")
            )
        )

        // announcement_line1, used for Goal overlay
        actionsList.add(
            Action.CreateVariableAction(
                id = "8",
                offset = 0L,
                absoluteTime = INVALID_TIME,
                variable = Variable.StringVariable("\$announcement_line1", "Goal")
            )
        )

        return actionsList
    }

    val testEvent = EventEntity(
        id = "ckzpd2purs5290jfsla2ddst1",
        title = "Brescia Calcio Femminile vs Pro Sesto Femminile",
        description = "Brescia Calcio Femminile vs Pro Sesto Femminile - Serie B Femminile",
        thumbnailUrl = "https://m-tv.imgix.net/07d68ec5a2469a64ef15e3a9c381bd1ff89b8d08b0ea0271fe3067361c6743df.jpg",
        poster_url = null,
        location = null,
        organiser = null,
        start_time = DateTime.parse("2021-11-14T14:30:00.000+01:00"),
        status = EventStatus.EVENT_STATUS_FINISHED,
        streams = listOf(
            Stream(
                id = "1",
                fullUrl = "https://europe-west-hls.mls.mycujoo.tv/esgp/ckzpd2rgw2vjj0152zpdvuhei/master.m3u8",
                widevine = null,
                dvrWindowString = null
            )
        ),
        timezone = null,
        timeline_ids = listOf(),
        metadata = null,
        is_test = false,
        isNativeMLS = false
    )

    @Module
    @InstallIn(SingletonComponent::class)
    class TestNetworkModule {

        @Singleton
        @Provides
        fun provideMlsApi(): MlsApi {
            return object : MlsApi {
                override suspend fun getEventDetails(
                    id: String,
                    updateId: String?
                ): EventSourceData {
                    return EventSourceData(
                        id = "1",
                        timeline_ids = listOf(),
                        title = "title",
                        description = "description",
                        is_test = true,
                        locationSourceData = LocationSourceData(
                            physicalSourceData = PhysicalSourceData(
                                city = "Amsterdam",
                                continent_code = "de",
                                coordinates = CoordinatesSourceData(
                                    latitude = 0.0,
                                    longitude = 0.0
                                ),
                                country_code = "nl",
                                venue = "Venue"
                            )
                        ),
                        metadata = MetadataSourceData(),
                        organiser = "Organiser",
                        poster_url = null,
                        start_time = DateTime.now().toString(),
                        status = "AVAILABLE",
                        streams = listOf(
                            StreamSourceData(
                                id = "1",
                                dvrWindowString = "",
                                drm = null,
                                fullUrl = "1",
                                errorCodeAndMessage = ErrorCodeAndMessageSourceData(
                                    code = ServerConstants.ERROR_CODE_NO_ENTITLEMENT,
                                    message = ServerConstants.ERROR_CODE_NO_ENTITLEMENT
                                )
                            )
                        ),
                        thumbnailUrl = "url",
                        timezone = ""
                    )
                }

                override suspend fun getActions(
                    timelineId: String,
                    updateId: String?
                ): ActionResponse {
                    return ActionResponse(
                        data = listOf()
                    )
                }

                override suspend fun getEvents(
                    pageSize: Int?,
                    pageToken: String?,
                    status: List<String>?,
                    orderBy: String?
                ): EventsSourceData {
                    return EventsSourceData(
                        events = listOf(),
                        previousPageToken = null,
                        nextPageToken = null
                    )
                }
            }
        }

        @Singleton
        @Provides
        fun provideOkHttp(): OkHttpClient {
            return OkHttpClient()
        }
    }
}
