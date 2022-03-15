package tv.mycujoo.e2e

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
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
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.joda.time.DateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.EventStatus
import tv.mycujoo.domain.entity.Stream
import tv.mycujoo.mcls.di.*
import tv.mycujoo.mcls.network.MlsApi
import tv.mycujoo.mcls.network.socket.*
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.utils.UuidUtils
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModuleBinds::class, PlayerModule::class, NetworkModule::class)
class LimitExceededWhenPlayingEvent : E2ETest() {

    private val videoIdlingResource = CountingIdlingResource("VIDEO")
    private val socketResources = CountingIdlingResource("NETWORK")
    private val videoResourceHelper = IdlingResourceHelper(videoIdlingResource)
    private val socketResourcesHelper = IdlingResourceHelper(socketResources)

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
        IdlingRegistry.getInstance().register(socketResources)

        socketResourcesHelper.setTimeoutLimit(25000)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(videoIdlingResource)
        IdlingRegistry.getInstance().unregister(socketResources)
    }

    @Test
    fun testLimitExceeded() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        UiThreadStatement.runOnUiThread {
                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    Timber.d("Sending Limit Exceeded")
                                    webSocket.send("concurrencyLimitExceeded;LIMIT")
                                }, 7000
                            )
                        }
                    }
                })
            }
        }

        videoIdlingResource.increment()
        socketResources.increment()

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying && !videoIdlingResource.isIdleNow) videoIdlingResource.decrement()

                Timber.d("$exoPlayer")
            }
        })

        UiThreadStatement.runOnUiThread {
            mMLS.getVideoPlayer().playVideo(testEvent)
        }

        videoResourceHelper.waitUntilIdle()
        socketResourcesHelper.waitUntilIdle()

        Thread.sleep(10000)

    }

    companion object {
        val mockWebServer = MockWebServer()

        val testEvent = EventEntity(
            id = "ckzpd2purs5290jfsla2ddst1",
            title = "Brescia Calcio Femminile vs Pro Sesto Femminile",
            description = "Brescia Calcio Femminile vs Pro Sesto Femminile - Serie B Femminile",
            thumbnailUrl = "https://m-tv.imgix.net/07d68ec5a2469a64ef15e3a9c381bd1ff89b8d08b0ea0271fe3067361c6743df.jpg",
            poster_url = null,
            location = null,
            organiser = null,
            start_time = DateTime.parse("2021-11-14T14:30:00.000+01:00"),
            status = EventStatus.EVENT_STATUS_FINISHED,
            streams = listOf(
                Stream(
                    id = "1",
                    fullUrl = "https://europe-west-hls.mls.mycujoo.tv/esgp/ckzpd2rgw2vjj0152zpdvuhei/master.m3u8",
                    widevine = null,
                    dvrWindowString = null
                )
            ),
            timezone = null,
            timeline_ids = listOf(),
            metadata = null,
            is_test = false,
            isNativeMLS = false
        )

        val context = InstrumentationRegistry.getInstrumentation().context
        val exoPlayer = ExoPlayer.Builder(context).build()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class NetworkModule {

        @Singleton
        @Provides
        fun context(): Context = context

        @Singleton
        @Provides
        fun provideExoPlayer(): ExoPlayer = exoPlayer

        @ConcurrencySocketUrl
        @Provides
        @Singleton
        fun provideConcurrencySocketUrl(): String =
            mockWebServer.url("/provideConcurrencySocketUrl").toString()

        @ReactorUrl
        @Provides
        @Singleton
        fun provideReactorSocketUrl(): String =
            mockWebServer.url("/provideReactorSocketUrl").toString()

        @Singleton
        @Provides
        fun provideRetrofit(): Retrofit {
            return Retrofit
                .Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl(PullingActionsInLiveEvent.mockWebServer.url("/"))
                .build()
        }

        @Singleton
        @Provides
        fun provideMlsApi(retrofit: Retrofit): MlsApi {
            return retrofit.create(MlsApi::class.java)
        }

        @Singleton
        @Provides
        fun provideOkHttp(): OkHttpClient {
            return OkHttpClient()
        }

        @Singleton
        @Provides
        fun provideConcurrencySocket(
            okHttpClient: OkHttpClient,
            mainSocketListener: MainWebSocketListener,
            userPreferencesUtils: UserPreferencesUtils,
        ): IConcurrencySocket {
            return ConcurrencySocket(
                okHttpClient,
                mainSocketListener,
                userPreferencesUtils,
                mockWebServer.url("/concurrency").toString()
            )
        }

        @Singleton
        @Provides
        fun provideReactorSocket(
            okHttpClient: OkHttpClient,
            mainSocketListener: MainWebSocketListener,
            uuidUtils: UuidUtils
        ): IReactorSocket {
            return ReactorSocket(
                okHttpClient,
                mainSocketListener,
                uuidUtils,
                mockWebServer.url("/reactor").toString()
            )
        }
    }
}