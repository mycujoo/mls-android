package tv.mycujoo.mls.widgets

import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.google.android.exoplayer2.ui.PlayerControlView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.domain.entity.*
import tv.mycujoo.fake.FakeAnimationFactory
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.defaultVideoPlayerConfig
import tv.mycujoo.mls.core.VideoPlayerMediator
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mls.enum.LogLevel
import tv.mycujoo.mls.helper.OverlayFactory
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.model.JoinTimelineParam
import tv.mycujoo.mls.model.SingleLiveEvent
import tv.mycujoo.mls.network.socket.IReactorSocket
import tv.mycujoo.mls.network.socket.ReactorCallback
import tv.mycujoo.mls.player.IPlayer
import tv.mycujoo.mls.player.MediaOnLoadCompletedListener
import tv.mycujoo.mls.player.Player
import tv.mycujoo.mls.player.Player.Companion.createExoPlayer
import tv.mycujoo.mls.player.Player.Companion.createMediaFactory


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class MLSPlayerViewTest {

    private lateinit var MLSPlayerView: MLSPlayerView
    private var viewHandler = ViewHandler(
        GlobalScope,
        CountingIdlingResource("ViewIdentifierManager")
    )

    private var animationHelper = FakeAnimationFactory()

    private lateinit var videoPlayerMediator: VideoPlayerMediator
    private lateinit var player: IPlayer

    private lateinit var MLSBuilder: MLSBuilder


    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            MLSPlayerView = MLSPlayerView(frameLayout.context)
            MLSPlayerView.id = View.generateViewId()
            frameLayout.addView(MLSPlayerView)

            MLSPlayerView.idlingResource = viewHandler.idlingResource
            MLSPlayerView.prepare(
                OverlayViewHelper(viewHandler, OverlayFactory(), animationHelper),
                viewHandler,
                emptyList()
            )


            MLSBuilder = MLSBuilder().withActivity(activity).publicKey("key_0")
            MLSBuilder.build()
        }
    }

    private fun setupPlayer() {
        val dataManager = object : IDataManager {

            override fun setLogLevel(logLevel: LogLevel) {
                TODO("Not yet implemented")
            }

            override suspend fun getEventDetails(
                eventId: String,
                updateId: String?
            ): Result<Exception, EventEntity> {
                TODO("Not yet implemented")
            }

            override fun getEventsLiveData(): SingleLiveEvent<List<EventEntity>> {
                TODO("Not yet implemented")
            }

            override fun fetchEvents(
                pageSize: Int?,
                pageToken: String?,
                eventStatus: List<EventStatus>?,
                orderBy: OrderByEventsParam?,
                fetchEventCallback: ((eventList: List<EventEntity>, previousPageToken: String, nextPageToken: String) -> Unit)?
            ) {
                TODO("Not yet implemented")
            }

            override suspend fun getActions(
                timelineId: String,
                updateId: String?
            ): Result<Exception, ActionResponse> {
                TODO("Not yet implemented")
            }

            override var currentEvent: EventEntity?
                get() = getSampleEventEntity(emptyList())
                set(value) {}

        }

        val reactorSocket = object : IReactorSocket {

            override fun setUUID(uuid: String) {
            }

            override fun joinEvent(eventId: String) {
            }

            override fun joinTimeline(param: JoinTimelineParam) {
                TODO("Not yet implemented")
            }

            override fun leave(destroyAfter: Boolean) {
            }

            override fun addListener(reactorCallback: ReactorCallback) {

            }
        }

        UiThreadStatement.runOnUiThread {
            player = Player()
            val exoPlayer = createExoPlayer(MLSPlayerView.context)
            player.create(
                createMediaFactory(MLSPlayerView.context),
                exoPlayer,
                MediaOnLoadCompletedListener(exoPlayer)
            )

            videoPlayerMediator = VideoPlayerMediator(
                defaultVideoPlayerConfig(),
                viewHandler,
                reactorSocket,
                GlobalScope,
                dataManager,
                emptyList(),
                null,
                Logger(LogLevel.MINIMAL)
            )
            videoPlayerMediator.initialize(
                MLSPlayerView,
                player,
                MLSBuilder
            )

            videoPlayerMediator.attachPlayer(MLSPlayerView)
        }

    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(viewHandler.idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(viewHandler.idlingResource)
    }

    @Test
    fun initializing_withoutLoadOrPlayVideo_shouldHideLoadingProgressBar() {
        setupPlayer()

        onView(withId(R.id.controller_buffering)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)))
    }

    @Test
    fun initializing_withoutLoadOrPlayVideo_shouldHideController() {
        setupPlayer()


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
    }

    @Test
    fun displayEventInfoForPreEvent_shouldDisplayEventInfoWithData() {
        MLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        MLSPlayerView.showEventInformationPreEventDialog()


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun displayEventInfoForPreEvent_shouldDisplayEventInfoWithPoster() {
        MLSPlayerView.setPosterInfo("sample_url")


        MLSPlayerView.showEventInformationPreEventDialog()


        onView(withId(R.id.eventInfoPreEventDialog_posterView)).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )
        onView(withId(R.id.eventInfoPreEventDialog_canvasView)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun whileDisplayingPreEventDialog_shouldNotTogglePlayerVisibilityOnClick() {
        MLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        MLSPlayerView.showEventInformationPreEventDialog()
        onView(withText("title_0")).perform(click())


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun displayEventInfoForStartedEvent_shouldDisplayEventInfo() {
        MLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        MLSPlayerView.showEventInfoForStartedEvents()


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }


    @Test
    fun whileDisplayingStartedEventDialog_shouldDismissDialogOnClick() {
        MLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        MLSPlayerView.showEventInfoForStartedEvents()
        onView(withText("title_0")).perform(click())


        onView(withText("title_0")).check(
            doesNotExist()
        )
        onView(withText("desc_0")).check(
            doesNotExist()
        )
    }

    @Test
    fun beforeSetupOfPlayer_PlayerControlViewShouldBeGone() {
        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
    }

    @Test
    fun whileDisplayingStartedEventDialog_shouldTogglePlayerVisibilityOnClick() {
        MLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")
        setupPlayer()
        MLSPlayerView.showEventInfoForStartedEvents()


        onView(withText("title_0")).perform(click())


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(withEffectiveVisibility(Visibility.VISIBLE))
        )
    }


    @Test
    fun clickOnEventInfoButton_shouldDisplayEventInfo() {
        MLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")
        setupPlayer()



        UiThreadStatement.runOnUiThread {
            MLSPlayerView.findViewById<ImageButton>(R.id.controller_informationButton)
                .performClick()
        }


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun showCastButton() {
        setupPlayer()

        MLSPlayerView.config(
            VideoPlayerConfig(
                primaryColor = "#FFFFFF",
                secondaryColor = "#000000",
                autoPlay = true,
                enableControls = true,
                showPlayPauseButtons = true,
                showBackForwardsButtons = true,
                showSeekBar = true,
                showTimers = true,
                showFullScreenButton = true,
                showLiveViewers = true,
                showEventInfoButton = true,
                showCastButton = false
            )
        )


        onView(withId(R.id.controller_castImageButtonContainer)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun hideCastButton() {
        setupPlayer()

        MLSPlayerView.config(
            VideoPlayerConfig(
                primaryColor = "#FFFFFF",
                secondaryColor = "#000000",
                autoPlay = true,
                enableControls = true,
                showPlayPauseButtons = true,
                showBackForwardsButtons = true,
                showSeekBar = true,
                showTimers = true,
                showFullScreenButton = true,
                showLiveViewers = true,
                showEventInfoButton = true,
                showCastButton = false
            )
        )


        onView(withId(R.id.controller_castImageButtonContainer)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    fun forceClick(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isClickable(), isEnabled(), isDisplayed())
            }

            override fun getDescription(): String {
                return "force click"
            }

            override fun perform(uiController: UiController, view: View) {
                view.performClick() // perform click without checking view coordinates.
                uiController.loopMainThreadUntilIdle()
            }
        }
    }

    /**region Fake data*/
    companion object {
        private fun getSampleStreamList(): List<Stream> {
            return listOf(Stream("stream_id_0", Long.MAX_VALUE, "stream_url", null))
        }

        fun getSampleEventEntity(streams: List<Stream>): EventEntity {
//        EventEntity(id=1eUBgUbXhriLFCT6A8E5a6Lv0R7, title=Test Title 0, description=Desc txt, thumbnail_url=,
//        location=Location(physical=Physical(city=Amsterdam, continent_code=EU, coordinates=Coordinates(latitude=52.3666969, longitude=4.8945398), country_code=NL, venue=)),
//        organiser=Org text, start_time=2020-07-11T07:32:46Z, status=EVENT_STATUS_SCHEDULED, streams=[Stream(fullUrl=https://rendered-europe-west.mls.mycujoo.tv/shervin/ckcfwmo4g000j0131mvc1zchu/master.m3u8)],
//        timezone=America/Los_Angeles, timeline_ids=[], metadata=tv.mycujoo.domain.entity.Metadata@ea3de11, is_test=false)

            val location =
                Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
            return EventEntity(
                "42",
                "",
                "",
                "",
                null,
                location,
                "",
                "",
                EventStatus.EVENT_STATUS_UNSPECIFIED,
                streams,
                "",
                emptyList(),
                Metadata(),
                false
            )
        }
    }

    /**endregion */


}