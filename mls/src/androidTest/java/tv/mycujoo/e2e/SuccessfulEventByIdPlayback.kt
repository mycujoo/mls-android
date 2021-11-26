package tv.mycujoo.e2e

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test
import tv.mycujoo.E2ETest
import tv.mycujoo.data.entity.ActionResponse
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class SuccessfulEventByIdPlayback : E2ETest() {

    @Test
    fun testInitialStartup() {
        Espresso.onView(ViewMatchers.withId(mMLSPlayerView.id)).check(
            ViewAssertions.matches(ViewMatchers.isDisplayed())
        )

//        mMLS.getVideoPlayer().playVideo("")

        Thread.sleep(10000)
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class TestNetworkModule {
        @Singleton
        @Provides
        fun provideMlsApi(): MlsApi {
            return object: MlsApi {
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
                                coordinates = CoordinatesSourceData(latitude = 0.0, longitude = 0.0),
                                country_code = "nl",
                                venue = "Venue"
                            )
                        ),
                        metadata = MetadataSourceData(),
                        organiser = "Organiser",
                        poster_url = null,
                        start_time = DateTime.now().toString(),
                        status = "AVAILABLE",
                        streams = listOf(),
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

            }
        }


        @Singleton
        @Provides
        fun provideOkHttp(): OkHttpClient {
            return OkHttpClient()
        }
    }
}

