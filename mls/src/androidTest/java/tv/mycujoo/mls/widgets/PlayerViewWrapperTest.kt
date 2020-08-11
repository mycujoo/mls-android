package tv.mycujoo.mls.widgets

import android.content.Intent
import android.net.Uri
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
import tv.mycujoo.domain.entity.*
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.domain.usecase.GetActionsFromJSONUseCase
import tv.mycujoo.fake.FakeAnimationFactory
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.mls.BlankActivity
import tv.mycujoo.mls.R
import tv.mycujoo.mls.api.MLSBuilder
import tv.mycujoo.mls.api.defaultVideoPlayerConfig
import tv.mycujoo.mls.core.VideoPlayerCoordinator
import tv.mycujoo.mls.data.IDataHolder
import tv.mycujoo.mls.helper.OverlayViewHelper
import tv.mycujoo.mls.manager.ViewIdentifierManager
import tv.mycujoo.mls.model.Event


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PlayerViewWrapperTest {

    private lateinit var playerViewWrapper: PlayerViewWrapper
    private var viewIdentifierManager = ViewIdentifierManager(
        GlobalScope,
        CountingIdlingResource("ViewIdentifierManager")
    )

    private var animationHelper = FakeAnimationFactory()

    private lateinit var videoPlayerCoordinator: VideoPlayerCoordinator
    lateinit var MLSBuilder: MLSBuilder


    @Before
    fun setUp() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), BlankActivity::class.java)
        val scenario = launchActivity<BlankActivity>(intent)
        scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            playerViewWrapper = PlayerViewWrapper(frameLayout.context)
            playerViewWrapper.id = View.generateViewId()
            frameLayout.addView(playerViewWrapper)

            playerViewWrapper.idlingResource = viewIdentifierManager.idlingResource
            playerViewWrapper.prepare(
                OverlayViewHelper(animationHelper),
                viewIdentifierManager,
                emptyList()
            )


            MLSBuilder = MLSBuilder().withActivity(activity).publicKey("key_0")
        }
    }

    private fun setupPlayer() {

        val eventRepository = object : EventsRepository {
            override suspend fun getEventsList(): Result<Exception, Events> {
                return Result.Success(Events(emptyList()))
            }

            override suspend fun getEventDetails(eventId: String): Result<Exception, EventEntity> {
                return Result.Success(getSampleEventEntity(emptyList()))
            }
        }

        val dataHolder = object : IDataHolder {
            override fun getEvent(): Event? {
                return getSampleEvent()
            }
        }



        videoPlayerCoordinator = VideoPlayerCoordinator(
            defaultVideoPlayerConfig(),
            viewIdentifierManager,
            GlobalScope,
            eventRepository,
            dataHolder,
            GetActionsFromJSONUseCase.mappedActionCollections().timelineMarkerActionList
        )
        videoPlayerCoordinator.initialize(playerViewWrapper, MLSBuilder)

        UiThreadStatement.runOnUiThread { videoPlayerCoordinator.attachPlayer(playerViewWrapper) }

    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(viewIdentifierManager.idlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(viewIdentifierManager.idlingResource)
    }

    @Test
    fun initializing_withoutLoadOrPlayVideo_shouldHideController() {
        setupPlayer()


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
    }

    @Test
    fun displayEventInfoForPreEvent_shouldDisplayEventInfo() {
        playerViewWrapper.setEventInfo("title_0", "desc_0")


        playerViewWrapper.displayEventInformationPreEventDialog()


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun whileDisplayingPreEventDialog_shouldNotTogglePlayerVisibilityOnClick() {
        playerViewWrapper.setEventInfo("title_0", "desc_0")


        playerViewWrapper.displayEventInformationPreEventDialog()
        onView(withText("title_0")).perform(click())


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun displayEventInfoForStartedEvent_shouldDisplayEventInfo() {
        playerViewWrapper.setEventInfo("title_0", "desc_0")


        playerViewWrapper.displayEventInfoForStartedEvents()


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }


    @Test
    fun whileDisplayingStartedEventDialog_shouldDismissDialogOnClick() {
        playerViewWrapper.setEventInfo("title_0", "desc_0")


        playerViewWrapper.displayEventInfoForStartedEvents()
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
        playerViewWrapper.setEventInfo("title_0", "desc_0")
        setupPlayer()
        playerViewWrapper.displayEventInfoForStartedEvents()


        onView(withText("title_0")).perform(click())


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(withEffectiveVisibility(Visibility.VISIBLE))
        )
    }


    @Test
    fun clickOnEventInfoButton_shouldDisplayEventInfo() {
        playerViewWrapper.setEventInfo("title_0", "desc_0")
        setupPlayer()



        UiThreadStatement.runOnUiThread {
            playerViewWrapper.findViewById<ImageButton>(R.id.controller_informationButton).performClick()
        }


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
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
            return listOf(Stream("stream_url"))
        }

        fun getSampleEventEntity(streams: List<Stream>): EventEntity {
//        EventEntity(id=1eUBgUbXhriLFCT6A8E5a6Lv0R7, title=Test Title 0, description=Desc txt, thumbnail_url=,
//        location=Location(physical=Physical(city=Amsterdam, continent_code=EU, coordinates=Coordinates(latitude=52.3666969, longitude=4.8945398), country_code=NL, venue=)),
//        organiser=Org text, start_time=2020-07-11T07:32:46Z, status=EVENT_STATUS_SCHEDULED, streams=[Stream(fullUrl=https://rendered-europe-west.mls.mycujoo.tv/shervin/ckcfwmo4g000j0131mvc1zchu/master.m3u8)],
//        timezone=America/Los_Angeles, timeline_ids=[], metadata=tv.mycujoo.domain.entity.Metadata@ea3de11, is_test=false)

            val location = Location(Physical("", "", Coordinates(0.toDouble(), 0.toDouble()), "", ""))
            return EventEntity(
                "42",
                "",
                "",
                "",
                location,
                "",
                "",
                "",
                streams,
                "",
                emptyList(),
                Metadata(),
                false
            )
        }


        fun getSampleEvent(): Event {
            return Event(
                "id_0",
                tv.mycujoo.mls.model.Stream(
                    listOf(Uri.parse("111"))
                ),
                "name_0",
                "location",
                "status"
            )
        }
    }

    /**endregion */


}