package tv.mycujoo.e2e

import android.content.Context
import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.QueueDispatcher
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tv.mycujoo.E2ETest
import tv.mycujoo.IdlingResourceHelper
import tv.mycujoo.data.jsonadapter.JodaJsonAdapter
import tv.mycujoo.domain.entity.EventEntity
import tv.mycujoo.domain.entity.Result
import tv.mycujoo.domain.params.EventIdPairParam
import tv.mycujoo.domain.usecase.GetEventDetailUseCase
import tv.mycujoo.mcls.di.MLSAPI
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.di.PublicApi
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.network.MlsApi
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton


@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class TestConstructionOfAuthorizationToken : E2ETest() {

    @BindValue
    val mockWebServer = MockWebServer()

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var getEventDetails: GetEventDetailUseCase

    private val networkIdling = CountingIdlingResource("VIDEO")

    val helper = IdlingResourceHelper(networkIdling)

    /**
     * Due to How Mapping Designed in this test, We have 2 cases should be asserted
     * The First is 403 GenericError, This is BAD HEADER Error
     * The Second is EOFException, This is a GOOD HEADER with empty JSON.
     */

    @Test
    fun testPhoneSDKAuthorizationHeaderConstruction() {
        mockWebServer.dispatcher = object : QueueDispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                Log.d("TAG", "dispatch: ${request.headers["Authorization"]}")
                return if (request.headers["Authorization"] == "Bearer publicKey,ID") {
                    MockResponse().setResponseCode(200)
                } else {
                    MockResponse().setResponseCode(403)
                }
            }
        }

        mMLS.setIdentityToken("ID")

        runBlocking {
            when (getEventDetails.execute(EventIdPairParam("event"))) {
                is Result.GenericError -> assert(false) { "BAD HEADER" }
                is Result.NetworkError -> assert(true)
                is Result.Success -> assert(false)
            }
        }

        mockWebServer.dispatcher = object : QueueDispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                Log.d("TAG", "dispatch: ${request.headers["Authorization"]}")
                return if (request.headers["Authorization"] == "Bearer publicKey") {
                    MockResponse().setResponseCode(200)
                } else {
                    MockResponse().setResponseCode(403)
                }
            }
        }

        mMLS.removeIdentityToken()

        runBlocking {
            when (getEventDetails.execute(EventIdPairParam("event"))) {
                is Result.GenericError -> assert(false) { "BAD HEADER" }
                is Result.NetworkError -> assert(true)
                is Result.Success -> assert(false)
            }
        }

        mockWebServer.dispatcher = object : QueueDispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                Log.d("TAG", "dispatch: ${request.headers["Authorization"]}")
                return if (request.headers["Authorization"] == "Bearer publicKey,ID") {
                    MockResponse().setResponseCode(200)
                } else {
                    MockResponse().setResponseCode(403)
                }
            }
        }

        mMLS.setIdentityToken("ID")

        runBlocking {
            when (getEventDetails.execute(EventIdPairParam("event"))) {
                is Result.GenericError -> assert(false) { "BAD HEADER" }
                is Result.NetworkError -> assert(true)
                is Result.Success -> assert(false)
            }
        }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    open class TestNetworkModule {

        private val maxAgeInSecond: Int = 60 * 5

        @Provides
        @Singleton
        open fun provideOkHttp(
            prefManager: IPrefManager,
            @ApplicationContext context: Context
        ): OkHttpClient {

            val cacheSize = 10 * 1024 * 1024 // 10 MiB
            val cache = Cache(context.cacheDir, cacheSize.toLong())

            val okHttpBuilder = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addInterceptor { chain: Interceptor.Chain ->
                    var authorizationHeader = "Bearer ${prefManager.get(C.PUBLIC_KEY_PREF_KEY)}"

                    if (prefManager.get(C.IDENTITY_TOKEN_PREF_KEY).isNullOrEmpty().not()) {
                        authorizationHeader += ",${prefManager.get(C.IDENTITY_TOKEN_PREF_KEY)}"
                    }

                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", authorizationHeader)
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .addHeader("Cache-Control", "public, max-age=$maxAgeInSecond")
                        .build()
                    val requestBody = chain.request().body
                    if (requestBody != null) {
                        Log.d(
                            "NetworkModule",
                            "intercept: " + chain.request().method + " " + chain.request().url
                        )
                        val buffer = Buffer()
                        requestBody.writeTo(buffer)
                        val charset = Charset.forName("UTF-8")
                        val contentType = requestBody.contentType()
                        if (contentType != null) {
                            Log.d("NetworkModule", "intercept: " + buffer.readString(charset))
                        }
                    }
                    chain.proceed(newRequest)
                }
                .addInterceptor(HttpLoggingInterceptor())
                .cache(cache)

            return okHttpBuilder.build()
        }

        @Provides
        @PublicApi
        @Singleton
        open fun provideRetrofit(
            okHttpClient: OkHttpClient,
            mockWebServer: MockWebServer
        ): Retrofit {
            val moshi: Moshi = Moshi.Builder()
                .add(JodaJsonAdapter())
                .build()

            return Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(okHttpClient)
                .build()
        }

        @Provides
        @MLSAPI
        @Singleton
        fun provideMlsApiRetrofit(
            okHttpClient: OkHttpClient,
            mockWebServer: MockWebServer
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build()
        }

        @Provides
        @Singleton
        fun provideMlsApi(@MLSAPI retrofit: Retrofit): MlsApi {
            return retrofit.create(MlsApi::class.java)
        }
    }
}