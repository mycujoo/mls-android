package tv.mycujoo.e2e

import android.content.Context
import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.squareup.moshi.Moshi
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
import tv.mycujoo.data.entity.ServerConstants
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.entity.msc.VideoPlayerConfig
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
class WhenChangingConfigNoVideoShouldPlay : E2ETest() {
    private val TAG = "EventWithErrorShouldShowErrorDialog"

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    val videoIdlingResource = CountingIdlingResource("VIDEO")

    val helper = IdlingResourceHelper(videoIdlingResource)

    val event1 = EventSourceData(
        id = "ckw25ntnkxlam0hbqnfhx3gk0",
        title = "Top 10 Gol Serie C 2021/22 - 14^ Giornata",
        description = "I dieci gol più belli della 14^ giornata di Serie C",
        thumbnailUrl = "",
        poster_url = null,
        organiser = "",
        start_time = "2021-11-14T14:30:00.000+01:00",
        status = "EVENT_STATUS_FINISHED",
        streams = listOf(
            StreamSourceData(
                id = "ckw25ntnkxlam0hbqnfhx3gk0",
                dvrWindowString = "",
                fullUrl = "https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8",
                widevine = null,
                errorCodeAndMessage = null
            )
        ),
        timezone = "",
        timeline_ids = listOf(),
        metadata = MetadataSourceData(),
        is_test = false,
        locationSourceData = LocationSourceData(
            physicalSourceData = PhysicalSourceData(
                city = "",
                continent_code = "",
                country_code = "",
                venue = "",
                coordinates = CoordinatesSourceData(latitude = 0.0, longitude = 0.0)
            )
        )
    )

    val event2 = EventSourceData(
        id = "ckw25ntnkxlam0hbqnfhx3gk0",
        title = "Top 10 Gol Serie C 2021/22 - 14^ Giornata",
        description = "I dieci gol più belli della 14^ giornata di Serie C",
        thumbnailUrl = "",
        poster_url = null,
        organiser = "",
        start_time = "2021-11-14T14:30:00.000+01:00",
        status = "EVENT_STATUS_SCHEDULED",
        streams = listOf(),
        timezone = "",
        timeline_ids = listOf(),
        metadata = MetadataSourceData(),
        is_test = false,
        locationSourceData = LocationSourceData(
            physicalSourceData = PhysicalSourceData(
                city = "",
                continent_code = "",
                country_code = "",
                venue = "",
                coordinates = CoordinatesSourceData(latitude = 0.0, longitude = 0.0)
            )
        )
    )

    @Test
    fun whenChangingConfigNoVideoShouldPlay() {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(EventSourceData::class.java)

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (!videoIdlingResource.isIdleNow) videoIdlingResource.decrement()
            }
        })

        // When we choose the first event, with a playable status
        Log.d(TAG, "And the exoplayer should not be playing the first event")
        videoIdlingResource.increment()
        mMLS.getVideoPlayer().playVideo(adapter.toJson(event1))
        helper.waitUntilIdle()

        UiThreadStatement.runOnUiThread {
            // Then We should see the video we picked played in the player
            Log.d(TAG, "Then We should see the video we picked played in the player")
            assert(exoPlayer.isPlaying)
        }

        // Then We Play a second event with no streams
        Log.d(TAG, "Then We Play a second event with no streams")
        mMLS.getVideoPlayer().playVideo(adapter.toJson(event2))

        // The Player should be paused
        UiThreadStatement.runOnUiThread {
            Log.d(TAG, "The Player should be paused")
            assert(exoPlayer.isPlaying.not())
        }

        // When Changing Configs
        Log.d(TAG, "When Changing Configs")
        mMLS.getVideoPlayer().config(
            VideoPlayerConfig(
                primaryColor = "#0000ff",
                secondaryColor = "#fff000",
                autoPlay = true,
                enableControls = true,
                showPlayPauseButtons = true,
                showBackForwardsButtons = true,
                showSeekBar = true,
                showTimers = true,
                showFullScreenButton = true,
                showLiveViewers = true,
                showEventInfoButton = true
            )
        )

        // Then the Player Should be Not Auto-Play Past Events
        Log.d(TAG, "Then the Player Should be Not Auto-Play Past Events")
        UiThreadStatement.runOnUiThread {
            assert(exoPlayer.isPlaying.not())
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
                    val moshi = Moshi.Builder().build()
                    val adapter = moshi.adapter(EventSourceData::class.java)

                    return adapter.fromJson(id) ?: defaultEvent
                }

                val defaultEvent = EventSourceData(
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
                            widevine = null,
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
