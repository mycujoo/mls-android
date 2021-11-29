package tv.mycujoo.e2e

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.internal.runner.junit4.statement.UiThreadStatement
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.core.PlayerEventsListener
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class SwitchEventWhenDifferent : E2ETest() {

    private val registry = IdlingRegistry.getInstance()
    lateinit var globalIdlingResources: CountingIdlingResource

    @Before
    fun initIdleResourcing() {
        globalIdlingResources = CountingIdlingResource("GLOBAL")
        registry.register(globalIdlingResources)
    }

    @After
    fun detach() {
        registry.unregister(globalIdlingResources)
    }

    @Ignore("Work Under Progress")
    @Test
    fun whenPlayingAnEventVideoPlayerShouldPlayAnEventInsideExoPlayer() =
        UiThreadStatement.runOnUiThread {
            var expectedPlayerStatus = true


            globalIdlingResources.increment()

            val playerEventsListener =
                PlayerEventsListener(object : tv.mycujoo.mcls.api.PlayerEventsListener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        globalIdlingResources.decrement()
                        assert(playing)
                    }

                    override fun onPlayerStateChanged(playbackState: Int) {
                    }
                })


            mMLS.getVideoPlayer().setPlayerEventsListener(playerEventsListener)
            mMLS.getVideoPlayer().playVideo("1")
        }


    @Module
    @InstallIn(SingletonComponent::class)
    class TestNetworkModule {

        @Singleton
        @Provides
        fun provideMlsApi(): MlsApi {
            return object : MlsApi {

                // Actual Useful used request
                override suspend fun getEventDetails(
                    id: String,
                    updateId: String?
                ): EventSourceData {
                    return EventSourceData(
                        id = id,
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
                                id = "",
                                dvrWindowString = "",
                                fullUrl = "https://playlists.mycujoo.football/eu/ckvwbajqyr3hu0gbljp9k4t9w/master.m3u8",
                                widevine = null,
                                errorCodeAndMessage = null
                            )
                        ),
                        thumbnailUrl = "url",
                        timezone = "",
                        title = "Title"
                    )
                }

                /** region Unused Invocations*/
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

                override suspend fun getActions(
                    timelineId: String,
                    updateId: String?
                ): ActionResponse {
                    return ActionResponse(
                        data = listOf()
                    )
                }

                /** endregion */
            }
        }


        @Singleton
        @Provides
        fun provideOkHttp(): OkHttpClient {
            return OkHttpClient()
        }
    }
}