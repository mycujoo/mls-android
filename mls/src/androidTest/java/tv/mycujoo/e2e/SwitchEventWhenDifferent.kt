/**
 * Tests Successful Playback Scenario.
 * To use this test, we can request event by id, and pass the video url as the id.
 * This way, the test will inject the video url into the event.
 *
 * Example:
 * mMLS.getVideoPlayer().playVideo("https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8")
 *
 * Will result in the Response :
 * EventSourceData(
 *      id = "https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8",
 *      title = "title: https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8",
 *      description = "description: https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8",
 *      ...
 *      streams = listOf(
 *          StreamSourceData(id = "1",fullUrl = "https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8")
 *      )
 * )
 */

package tv.mycujoo.e2e

import android.content.Context
import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
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
import org.joda.time.DateTime
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.di.ConcurrencySocketUrl
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.di.ReactorUrl
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
class SwitchEventWhenDifferent : E2ETest() {

    private val TAG = "SwitchEventWhenDifferent"

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    val videoIdlingResource = CountingIdlingResource("VIDEO")

    val helper = IdlingResourceHelper(videoIdlingResource)

    @Test
    fun whenPlayerIsPlayingAndPlaySecondEventRequestedShouldSwitchVideoPlaying() {
        // Given we have 2 events shown in the screen
        Log.d(TAG, "Given we have 2 events shown in the screen")
        val firstEvent = "https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8"
        val secondEvent =
            "https://playlists.mycujoo.tv/match-recap/playlist.m3u8?eventId=ckvmcys5vgdi80hbq79txdzrs&highlightIds=ckw0w3v6l3ode15bhh50qbqi5,ckw0w5dm13lwp158620ipbgwx,ckw0wd3pd3n1s15b9cc5k50m5,ckw0wijlc3lx315864ujwbusl,ckw0wq4nv3lxb1586dgm5eco5,ckw0x38cr3oem15bh1ca5dclp,ckw0yio5z3n3c15b9bf128044,ckw0yuppa3m13158656zbaszv,ckw0z4snj3n7715b96lpvhgzp,ckw0zcw6t3m7a1586cgdi0he5,ckw0zrr8l3me81586bnrb7tex,ckw0zzvpy3nrz15b9hs5f0o6p,ckw108zv73ny915b9gf1l16qn,ckw10bt023mqu1586aszde2vo"

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                // Since there is a pause event, we should pause when playing happens only
                if (isPlaying) {
                    videoIdlingResource.decrement()
                }
            }
        })

        // When we choose the first event
        Log.d(TAG, "And the exoplayer should not be playing the first event")
        videoIdlingResource.increment()
        mMLS.getVideoPlayer().playVideo(firstEvent)
        helper.waitUntilIdle()

        // Then We should see the video we picked played in the player
        UiThreadStatement.runOnUiThread {
            Log.d(TAG, "Then We should see the video we picked played in the player")
            val firstMediaItem = exoPlayer.getMediaItemAt(0)
            assert(firstMediaItem.localConfiguration?.uri.toString() == firstEvent)
            assert(exoPlayer.isPlaying)
        }

        // When Pressing the info button
        Log.d(TAG, "When Pressing the info button")
        onView(withId(R.id.controller_informationButtonLayout))
            .perform(click())

        // Then I should see the first event info
        Log.d(TAG, "Then I should see the first event info")
        onView(withId(R.id.startedEventInfoDialog_titleTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.startedEventInfoDialog_bodyTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.startedEventInfoDialog_titleTextView))
            .check(matches(withText("title: $firstEvent")))
        onView(withId(R.id.startedEventInfoDialog_bodyTextView))
            .check(matches(withText("description: $firstEvent")))



        // When we choose the second event
        Log.d(TAG, "When we choose the second event")
        videoIdlingResource.increment()
        mMLS.getVideoPlayer().playVideo(secondEvent)
        helper.waitUntilIdle()

        // Then the exoplayer should play the second event
        UiThreadStatement.runOnUiThread {
            Log.d(TAG, "Then the exoplayer should play the second event")
            val secondMediaItem = exoPlayer.getMediaItemAt(0)
            assert(secondMediaItem.localConfiguration?.uri.toString() == secondEvent)
            assert(exoPlayer.isPlaying)
        }

        // When Pressing the info button
        Log.d(TAG, "When Pressing the info button")
        onView(withId(R.id.controller_informationButtonLayout))
            .perform(click())

        // Then I should see the second event info
        Log.d(TAG, "Then I should see the second event info")
        onView(withId(R.id.startedEventInfoDialog_titleTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.startedEventInfoDialog_bodyTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.startedEventInfoDialog_titleTextView))
            .check(matches(withText("title: $secondEvent")))
        onView(withId(R.id.startedEventInfoDialog_bodyTextView))
            .check(matches(withText("description: $secondEvent")))

        // And the exoplayer should not be playing the first event
        UiThreadStatement.runOnUiThread {
            Log.d(TAG, "And the exoplayer should NOT be playing the first event")
            val secondMediaItem = exoPlayer.getMediaItemAt(0)
            assert(secondMediaItem.localConfiguration?.uri.toString() != firstEvent)
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
                        id = id,
                        timeline_ids = listOf(),
                        title = "title: $id",
                        description = "description: $id",
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
                                id = "id",
                                dvrWindowString = "",
                                drm = null,
                                fullUrl = id
                            )
                        ),
                        thumbnailUrl = "url",
                        timezone = "",
                        is_protected = false,
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