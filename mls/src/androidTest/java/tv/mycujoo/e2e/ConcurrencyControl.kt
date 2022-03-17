package tv.mycujoo.e2e

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
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
import tv.mycujoo.mcls.R
import tv.mycujoo.mcls.di.*
import tv.mycujoo.mcls.network.MlsApi
import tv.mycujoo.mcls.network.socket.*
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.utils.UuidUtils
import javax.inject.Singleton

/**
 * Testing the SDK responses to BFF RT WebSocket Messages.
 * Currently we support 6 different responses
 *
 *      1. err;badRequest;-;invalidCommand
 *      2. err;badRequest;sessionId;missingIdentifier
 *      3. err;forbidden;identityToken;authFailed
 *      4. err;forbidden;identityToken;relogin
 *      5. err;preconditionFailed;identityToken;notEntitled
 *      6. err;internal;identityToken;internalServerError
 *      7. ok;identityToken
 *      8. concurrencyLimitExceeded;LIMIT
 */

@HiltAndroidTest
@UninstallModules(NetworkModuleBinds::class, PlayerModule::class, NetworkModule::class)
class ConcurrencyControl : E2ETest() {

    private val videoIdlingResource = CountingIdlingResource("VIDEO")
    private val videoResourceHelper = IdlingResourceHelper(videoIdlingResource)

    val exoPlayerHandler = Handler(Looper.getMainLooper())

    @Before
    fun setUp() {
        IdlingRegistry.getInstance().register(videoIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(videoIdlingResource)
    }

    /**
     * Testing concurrencyLimitExceeded;LIMIT Socket Response
     */

    @Test
    fun testLimitExceeded() {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                webSocket.send("concurrencyLimitExceeded;LIMIT")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(matches(isDisplayed()))

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(matches(withText("Device limit reached.")))
    }

    /**
     * Testing err;badRequest;sessionId;missingIdentifier
     */
    @Test
    fun testBadRequestMissingIdentifier() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("err;badRequest;sessionId;missingIdentifier")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        responsesCount shouldBeEqualTo 1
    }

    /**
     * Testing err;badRequest;-;invalidCommand
     */
    @Test
    fun testBadRequestInvalidCommand() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("err;badRequest;-;invalidCommand")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        responsesCount shouldBeEqualTo 1
    }

    /**
     * Testing err;forbidden;identityToken;authFailed
     */
    @Test
    fun testBadRequestAuthFailed() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("err;forbidden;identityToken;authFailed")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        responsesCount shouldBeEqualTo 1
    }

    /**
     * Testing err;forbidden;identityToken;relogin
     */
    @Test
    fun testBadRequestReLogin() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("err;forbidden;identityToken;relogin")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        responsesCount shouldBeEqualTo 1
    }

    /**
     * Testing err;preconditionFailed;identityToken;notEntitled
     */
    @Test
    fun testBadRequestNotEntitled() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("err;preconditionFailed;identityToken;notEntitled")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        responsesCount shouldBeEqualTo 1
    }

    /**
     * Testing err;internal;identityToken;internalServerError
     */
    @Test
    fun testInternalServerError() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("err;internal;identityToken;internalServerError")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(8000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        Timber.d(responsesCount.toString())

        responsesCount shouldBeGreaterThan 1
    }

    /**
     * Testing ok;identityToken
     */
    @Test
    fun testBadRequest() {
        var responsesCount = 0

        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        super.onOpen(webSocket, response)

                        exoPlayerHandler.postDelayed(
                            {
                                responsesCount++
                                webSocket.send("ok;identityToken")
                            }, 1000
                        )
                    }
                })
            }
        }

        videoIdlingResource.increment()

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

        Thread.sleep(1000)

        onView(withId(R.id.preEventInfoDialog_bodyTextView))
            .check(doesNotExist())

        responsesCount shouldBeEqualTo 1
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
            isNativeMLS = false,
            is_protected = true
        )

        val context: Context = InstrumentationRegistry.getInstrumentation().context
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
        ): IBFFRTSocket {
            return BFFRTSocket(
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