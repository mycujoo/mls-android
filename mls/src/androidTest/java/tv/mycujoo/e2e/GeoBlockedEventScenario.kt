package tv.mycujoo.e2e

import android.content.Context
import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.ExoPlayer
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
import tv.mycujoo.data.entity.ServerConstants.Companion.ERROR_CODE_GEOBLOCKED
import tv.mycujoo.data.model.*
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PlayerModule
import tv.mycujoo.mcls.network.MlsApi
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModule::class, PlayerModule::class)
class GeoBlockedEventScenario : E2ETest() {
    private val TAG = "EventWithErrorShouldShowErrorDialog"

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    private val videoIdlingResource = CountingIdlingResource("VIDEO")

    val helper = IdlingResourceHelper(videoIdlingResource)

    @Test
    fun testErrorDialog() {
        // when we play a geo-blocked event
        mMLS.getVideoPlayer().playVideo("s")

        // Then I should see the first event info
        Log.d(TAG, "Then I should see Geo Blocked Error in the screen")
        onView(withId(R.id.preEventInfoDialog_titleTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        onView(withId(R.id.preEventInfoDialog_titleTextView))
            .check(matches(withText("title")))
        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(matches(withText(context.getString(R.string.message_geoblocked_stream))))
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
                                    code = ERROR_CODE_GEOBLOCKED,
                                    message = ERROR_CODE_GEOBLOCKED
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