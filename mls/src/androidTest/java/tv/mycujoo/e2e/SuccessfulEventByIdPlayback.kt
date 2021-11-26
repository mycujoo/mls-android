package tv.mycujoo.e2e

import android.content.Context
import android.util.Log
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
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
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.network.MlsApi
import java.lang.Exception
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
@LargeTest
class SuccessfulEventByIdPlayback : E2ETest() {

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
    }

    @Test
    fun testInitialStartup() {
        videoIdlingResource.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                Log.d(TAG, "onIsPlayingChanged: $isPlaying")
                if (isPlaying) videoIdlingResource.decrement()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                super.onPlayWhenReadyChanged(playWhenReady, reason)
                Log.d(TAG, "onPlayWhenReadyChanged: $playWhenReady, $reason")
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.d(TAG, "onPlayerError: ${error.message} ${error.errorCode} ${error.cause}")
            }
        })

        val videoUrl = "https://vod-eu.mycujoo.tv/hls/ckw25mmlaxl8i0hbqp6wr5pft/master.m3u8"

        mMLS.getVideoPlayer().playVideo(videoUrl)


        while (!videoIdlingResource.isIdleNow) {
            Log.d(TAG, "testInitialStartup: looping")
            Thread.sleep(500)
        }

        UiThreadStatement.runOnUiThread {
            assert(exoPlayer.isPlaying)
        }
    }

    companion object {
        private const val TAG = "SuccessfulEventByIdPlay"

        @JvmStatic
        val videoIdlingResource = CountingIdlingResource("VIDEO")
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

