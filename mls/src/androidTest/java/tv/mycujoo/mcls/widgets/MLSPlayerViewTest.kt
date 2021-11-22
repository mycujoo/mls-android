package tv.mycujoo.mcls.widgets

import android.content.Context
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerControlView
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.android.synthetic.main.player_view_wrapper.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tv.mycujoo.TestActivity
import tv.mycujoo.fake.FakeAnimationFactory
import tv.mycujoo.matchers.TypeMatcher
import tv.mycujoo.mcls.BlankActivity
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.api.MLSBuilder
import tv.mycujoo.mcls.core.VideoPlayerMediator
import tv.mycujoo.mcls.helper.OverlayFactory
import tv.mycujoo.mcls.helper.OverlayViewHelper
import tv.mycujoo.mcls.manager.VariableKeeper
import tv.mycujoo.mcls.manager.VariableTranslator
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.player.MediaOnLoadCompletedListener
import tv.mycujoo.mcls.player.Player
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MLSPlayerViewTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var scenarioRule = activityScenarioRule<BlankActivity>()

    /** region Injects */
    @Inject
    @ApplicationContext
    lateinit var applicationContext: Context

    @Inject
    lateinit var viewHandler: ViewHandler

    @Inject
    lateinit var variableTranslator: VariableTranslator

    @Inject
    lateinit var variableKeeper: VariableKeeper

    @Inject
    lateinit var videoPlayerMediator: VideoPlayerMediator

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaFactory: MediaFactory

    @Inject
    lateinit var handler: Handler

    @Inject
    lateinit var mediaOnLoadCompletedListener: MediaOnLoadCompletedListener
    /** endregion */

    private lateinit var mMLSPlayerView: MLSPlayerView

    private var animationHelper = FakeAnimationFactory()
    private lateinit var player: IPlayer

    private lateinit var mMLSBuilder: MLSBuilder


    @Before
    fun setUp() {
        hiltRule.inject()

        scenarioRule.scenario.onActivity { activity ->
            val frameLayout = activity.findViewById<FrameLayout>(R.id.blankActivity_rootView)
            mMLSPlayerView = MLSPlayerView(frameLayout.context)
            mMLSPlayerView.id = View.generateViewId()
            frameLayout.addView(mMLSPlayerView)

            mMLSPlayerView.idlingResource = viewHandler.idlingResource

            mMLSPlayerView.prepare(
                OverlayViewHelper(
                    viewHandler,
                    OverlayFactory(),
                    animationHelper,
                    variableTranslator,
                    variableKeeper
                ),
                viewHandler,
                emptyList()
            )


            mMLSBuilder = MLSBuilder()
                .publicKey("key_0")
                .withActivity(activity)

            mMLSBuilder.build()
        }
    }

    private fun setupPlayer() {
        UiThreadStatement.runOnUiThread {
            player = Player(
                mediaFactory,
                exoPlayer,
                mediaOnLoadCompletedListener,
                handler
            )
            player.create(null)
            videoPlayerMediator.initialize(
                mMLSPlayerView,
                mMLSBuilder
            )

            videoPlayerMediator.attachPlayer(mMLSPlayerView)
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
    fun displayGeoBlockedDialogTest() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        mMLSPlayerView.showCustomInformationDialog(applicationContext.getString(R.string.message_geoblocked_stream))


        Thread.sleep(5000)

        onView(withText(applicationContext.getString(R.string.message_geoblocked_stream))).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )
    }

    @Test
    fun displayEventInfoForPreEvent_shouldDisplayEventInfoWithData() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        mMLSPlayerView.showPreEventInformationDialog()


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun displayEventInfoForPreEvent_shouldDisplayEventInfoWithPoster() {
        mMLSPlayerView.setPosterInfo("sample_url")


        mMLSPlayerView.showPreEventInformationDialog()


        onView(withId(R.id.eventInfoPreEventDialog_posterView)).check(
            matches(
                withEffectiveVisibility(Visibility.VISIBLE)
            )
        )
        onView(withId(R.id.preEventInfoDialog_textualLayout)).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun whileDisplayingPreEventDialog_shouldNotTogglePlayerVisibilityOnClick() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        mMLSPlayerView.showPreEventInformationDialog()
        onView(withText("title_0")).perform(click())


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(
                withEffectiveVisibility(Visibility.GONE)
            )
        )
    }

    @Test
    fun displayEventInfoForStartedEvent_shouldDisplayEventInfo() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        mMLSPlayerView.showStartedEventInformationDialog()


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun hideInfoDialogs_removedAllInfoDialogChildren() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")
        mMLSPlayerView.showCustomInformationDialog("Message")
        mMLSPlayerView.showPreEventInformationDialog()
        mMLSPlayerView.showStartedEventInformationDialog()

        mMLSPlayerView.hideInfoDialogs()

        onView(withId(mMLSPlayerView.infoDialogContainerLayout.id)).check(
            matches(
                hasChildCount(
                    0
                )
            )
        )
    }

    @Test
    fun whileDisplayingStartedEventDialog_shouldDismissDialogOnClick() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")


        mMLSPlayerView.showStartedEventInformationDialog()
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
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")
        setupPlayer()
        mMLSPlayerView.showStartedEventInformationDialog()


        onView(withText("title_0")).perform(click())


        onView(withClassName(TypeMatcher(PlayerControlView::class.java.canonicalName))).check(
            matches(withEffectiveVisibility(Visibility.GONE))
        )
    }


    @Test
    fun clickOnEventInfoButton_shouldDisplayEventInfo() {
        mMLSPlayerView.setEventInfo("title_0", "desc_0", "2020-07-11T07:32:46Z")
        setupPlayer()



        UiThreadStatement.runOnUiThread {
            mMLSPlayerView.findViewById<ImageButton>(R.id.controller_informationButton)
                .performClick()
        }


        onView(withText("title_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withText("desc_0")).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun whenControllerIsDisplayed_topContainerShouldBeDisplayed() {
        UiThreadStatement.runOnUiThread {
            setupPlayer()
            mMLSPlayerView.updateControllerVisibility(true)
        }

        onView(withId(R.id.controller_topRightContainer))
            .check(
                matches(
                    withEffectiveVisibility(Visibility.VISIBLE)
                )
            )
    }

    @Test
    fun whenControllerIsGone_topContainerShouldBeGone() {
        UiThreadStatement.runOnUiThread {
            setupPlayer()
            mMLSPlayerView.updateControllerVisibility(true)

            mMLSPlayerView.playerView.showController()
            mMLSPlayerView.playerView.hideController()
        }

        onView(withId(R.id.controller_topRightContainer))
            .check(
                matches(withEffectiveVisibility(Visibility.GONE))
            )
    }

    @Test
    fun givenViewToAddToTopRightContainer_shouldAddIt() {
        var id = 0
        UiThreadStatement.runOnUiThread {
            setupPlayer()
            mMLSPlayerView.updateControllerVisibility(true)
            val button = Button(mMLSPlayerView.context)
            button.text = "button"
            button.id = View.generateViewId()
            id = button.id


            mMLSPlayerView.addToTopRightContainer(button)
        }

        onView(withId(id)).check(matches(isDisplayed()))
    }

    @Test
    fun givenViewToAddFromTopRightContainer_shouldRemoveIt() {
        var id = 0
        UiThreadStatement.runOnUiThread {
            setupPlayer()
            mMLSPlayerView.updateControllerVisibility(true)
            val button = Button(mMLSPlayerView.context)
            button.text = "button"
            button.id = View.generateViewId()
            id = button.id
            mMLSPlayerView.addToTopRightContainer(button)


            mMLSPlayerView.removeFromTopRightContainer(button)
        }

        onView(withId(id)).check(doesNotExist())
    }

    @Test
    fun givenViewToAddToTopLeftContainer_shouldAddIt() {
        var id = 0
        UiThreadStatement.runOnUiThread {
            setupPlayer()
            mMLSPlayerView.updateControllerVisibility(true)
            val button = Button(mMLSPlayerView.context)
            button.text = "button"
            button.id = View.generateViewId()
            id = button.id


            mMLSPlayerView.addToTopLeftContainer(button)
        }

        onView(withId(id)).check(matches(isDisplayed()))
    }

    @Test
    fun givenViewToAddFromTopLeftContainer_shouldRemoveIt() {
        var id = 0
        UiThreadStatement.runOnUiThread {
            setupPlayer()
            mMLSPlayerView.updateControllerVisibility(true)
            val button = Button(mMLSPlayerView.context)
            button.text = "button"
            button.id = View.generateViewId()
            id = button.id
            mMLSPlayerView.addToTopLeftContainer(button)


            mMLSPlayerView.removeFromTopLeftContainer(button)
        }

        onView(withId(id)).check(doesNotExist())
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
    /**
    companion object {
        private fun getSampleStreamList(): List<Stream> {
            return listOf(Stream("stream_id_0", Long.MAX_VALUE.toString(), "stream_url", null))
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
                null,
                EventStatus.EVENT_STATUS_UNSPECIFIED,
                streams,
                "",
                emptyList(),
                Metadata(),
                false
            )
        }
    }
    */
    /**endregion */


}