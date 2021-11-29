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
 *      id = "1",
 *      ...
 *      streams = listOf(
 *          StreamSourceData(id = "1",fullUrl = "https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8")
 *      )
 * )
 */

package tv.mycujoo.e2e

import android.content.Context
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.filters.LargeTest
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
import org.junit.Before
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
@LargeTest
class SuccessfulEventByIdPlayback : E2ETest() {

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    val videoIdlingResource = CountingIdlingResource("VIDEO")

    val helper = IdlingResourceHelper(videoIdlingResource)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
    }

    @Test
    fun testPlayEvent() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) videoIdlingResource.decrement()
            }
        })

        mMLS.getVideoPlayer().playVideo("https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8")

        helper.waitUntilIdle()

        UiThreadStatement.runOnUiThread {
            assert(exoPlayer.isPlaying)
        }
    }

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
                        description = "Description",
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
                                widevine = null,
                                fullUrl = id
                            )
                        ),
                        thumbnailUrl = "url",
                        timezone = "",
                        title = "Title"
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

