package tv.mycujoo.e2e

import android.content.Context
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
import org.amshove.kluent.shouldBeLessOrEqualTo
import org.amshove.kluent.shouldNotBeIn
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.TestData
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.entity.ServerConstants
import tv.mycujoo.data.model.*
import tv.mycujoo.domain.entity.*
import tv.mycujoo.mcls.di.ConcurrencySocketUrl
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.di.ReactorUrl
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Inject
import javax.inject.Singleton

/**
 * For these tests, we are trying to figure out if the widget is inflated in the correct place.
 * At this stage, we are using a white-box testing method, where we know that the ViewGroup (ConstraintLayout)
 * is used for the inflation, and that the properties used to locate widgets in it are verticalBias, and HorizontalBias.
 *
 * In later stages, it would be better to use a black-box mentality, and using pixels to locate overlays.
 * Since there are a lot of calculations involved to convert biases to pixels, and the device
 * dependency of these calculations (screen density, and size) I choose to skip it for now.
 * This allows this test to run on any CI or emulator regardless of the density or dimensions.
 */
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

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(videoIdlingResource)
    }

    /**
     * Test the Positions of Scoreboard Overlays using the following guides:
     *      Top-Left
     */
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

        val actionsList = TestData.getSampleScoreboardActionsList()
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
                    offset = 7000,
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

        Thread.sleep(3000)
        
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo (position * 20f) / 100
        }

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(3000)
        
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo (position * 20f) / 100
        }
    }

    /**
     * Test the Positions of Scoreboard Overlays using the following guides:
     *      Top-Right
     */
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

        val actionsList = TestData.getSampleScoreboardActionsList()
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
                    offset = 7000,
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

        Thread.sleep(3000)
        
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(3000)
        
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo (position * 20f) / 100
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }
    }

    /**
     * Test the Positions of Scoreboard Overlays using the following guides:
     *      Bottom-Left
     */
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

        val actionsList = TestData.getSampleScoreboardActionsList()
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
                    offset = 7000,
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

        Thread.sleep(3000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo ((position * 20f) / 100)
        }
        

        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        Thread.sleep(3000)
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo ((position * 20f) / 100)
        }
        
    }

    /**
     * Test the Positions of Scoreboard Overlays using the following guides:
     *      Bottom-Right
     */
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

        val actionsList = TestData.getSampleScoreboardActionsList()
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
                    offset = 7000,
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

        Thread.sleep(3000)
        
        viewHandler.getOverlayHost().children.forEach {
            val position = "${it.tag}".toInt()
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.verticalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
            layoutParams.horizontalBias shouldBeEqualTo 1 - ((position * 20f) / 100)
        }
    }

    /**
     * Test the Positions of Scoreboard Overlays using the following guides:
     *      Top-Left (Position 0%, 0%)
     *      Top-Right (Position 20%, 20%)
     *      Bottom-Left Position (40%, 40%)
     *      Bottom-Right Position (60%, 60%)
     */
    @Test
    fun testOverlayPositionMixed() {
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

        val actionsList = TestData.getSampleScoreboardActionsList()

        // region Top Left Action
        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_topLeft",
                absoluteTime = 0,
                offset = 0,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(left = 0f, top = 0f),
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
                customId = "topLeft",
            )
        )

        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_topLeft",
                absoluteTime = 0,
                offset = 7000,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(left = 0f, top = 0f),
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
                customId = "topLeft",
            )
        )

        actionsList.add(
            Action.HideOverlayAction(
                id = "overlay_topLeft",
                absoluteTime = 0,
                offset = 5000,
                customId = "topLeft",
            )
        )
        // endregion

        // region Top Right Actions
        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_topRight",
                absoluteTime = 0,
                offset = 0,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(top = 20f, right = 20f),
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
                customId = "topRight",
            )
        )

        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_topRight",
                absoluteTime = 0,
                offset = 7000,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(top = 20f, right = 20f),
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
                customId = "topRight",
            )
        )

        actionsList.add(
            Action.HideOverlayAction(
                id = "overlay_topRight",
                absoluteTime = 0,
                offset = 5000,
                customId = "topRight",
            )
        )
        // endregion

        // region Bottom Left Action
        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_bottomLeft",
                absoluteTime = 0,
                offset = 0,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(left = 40f, bottom = 40f),
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
                customId = "bottomLeft",
            )
        )

        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_bottomLeft",
                absoluteTime = 0,
                offset = 7000,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(left = 40f, bottom = 40f),
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
                customId = "bottomLeft",
            )
        )

        actionsList.add(
            Action.HideOverlayAction(
                id = "overlay_bottomLeft",
                absoluteTime = 0,
                offset = 5000,
                customId = "bottomLeft",
            )
        )
        // endregion

        // region Bottom Right Action
        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_bottomRight",
                absoluteTime = 0,
                offset = 0,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(bottom = 60f, right = 60f),
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
                customId = "bottomRight",
            )
        )

        actionsList.add(
            Action.ShowOverlayAction(
                id = "overlay_bottomRight",
                absoluteTime = 0,
                offset = 7000,
                svgData = SvgData(
                    svgUrl = "https://mycujoo-static.imgix.net/eleven_svg_scoreboard.svg"
                ),
                viewSpec = ViewSpec(
                    PositionGuide(bottom = 60f, right = 60f),
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
                customId = "bottomRight",
            )
        )

        actionsList.add(
            Action.HideOverlayAction(
                id = "overlay_bottomRight",
                absoluteTime = 0,
                offset = 5000,
                customId = "bottomRight",
            )
        )
        // endregion

        mMLS.getVideoPlayer().setLocalAnnotations(actionsList)

        helper.waitUntilIdle()

        val epsilon = 0.05

        // Check initial Overlays
        Thread.sleep(3000)
        viewHandler.getOverlayHost().children.forEach {
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            when (it.tag) {
                "topLeft" -> {
                    layoutParams.verticalBias - 0.0 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.0 shouldBeLessOrEqualTo epsilon
                }
                "topRight" -> {
                    layoutParams.verticalBias - 0.2 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.8 shouldBeLessOrEqualTo epsilon
                }
                "bottomLeft" -> {
                    layoutParams.verticalBias - 0.6 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.4 shouldBeLessOrEqualTo epsilon
                }
                "bottomRight" -> {
                    layoutParams.verticalBias - 0.4 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.4 shouldBeLessOrEqualTo epsilon
                }
                else -> it shouldNotBeIn listOf("topLeft", "topRight", "bottomLeft", "bottomRight")
            }
        }
        

        // Check Overlays Removal
        Thread.sleep(3000)
        viewHandler.getOverlayHost().childCount shouldBeEqualTo 0

        // Check reappearing Overlays
        Thread.sleep(3000)
        viewHandler.getOverlayHost().children.forEach {
            val layoutParams = it.layoutParams as ConstraintLayout.LayoutParams
            when (it.tag) {
                "topLeft" -> {
                    layoutParams.verticalBias - 0.0 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.0 shouldBeLessOrEqualTo epsilon
                }
                "topRight" -> {
                    layoutParams.verticalBias - 0.2 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.8 shouldBeLessOrEqualTo epsilon
                }
                "bottomLeft" -> {
                    layoutParams.verticalBias - 0.6 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.4 shouldBeLessOrEqualTo epsilon
                }
                "bottomRight" -> {
                    layoutParams.verticalBias - 0.4 shouldBeLessOrEqualTo epsilon
                    layoutParams.horizontalBias - 0.4 shouldBeLessOrEqualTo epsilon
                }
                else -> it shouldNotBeIn listOf("topLeft", "topRight", "bottomLeft", "bottomRight")
            }
        }
        
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class TestNetworkModule {

        @ConcurrencySocketUrl
        @Provides
        @Singleton
        fun provideConcurrencySocketUrl(): String = "wss://bff-rt.mycujoo.tv"

        @ReactorUrl
        @Provides
        @Singleton
        fun provideReactorSocketUrl(): String = "wss://mls-rt.mycujoo.tv"

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
