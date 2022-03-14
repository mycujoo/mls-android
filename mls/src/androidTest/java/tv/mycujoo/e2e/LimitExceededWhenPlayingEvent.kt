package tv.mycujoo.e2e

import android.content.Context
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
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tv.mycujoo.E2ETest
import tv.mycujoo.mcls.di.*
import tv.mycujoo.mcls.network.MlsApi
import tv.mycujoo.mcls.network.socket.*
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import tv.mycujoo.mcls.utils.UuidUtils
import javax.inject.Singleton

@HiltAndroidTest
@UninstallModules(NetworkModuleBinds::class, PlayerModule::class, NetworkModule::class)
class LimitExceededWhenPlayingEvent : E2ETest() {

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()

    @Test
    fun testStartup() {
        Thread.sleep(20000)
    }

    companion object {
        val mockWebServer = MockWebServer()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class NetworkModule {
        @ConcurrencySocketUrl
        @Provides
        @Singleton
        fun provideConcurrencySocketUrl(): String = mockWebServer.url("/provideConcurrencySocketUrl").toString()

        @ReactorUrl
        @Provides
        @Singleton
        fun provideReactorSocketUrl(): String = mockWebServer.url("/provideReactorSocketUrl").toString()

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
                mockWebServer.url("/concurrency/").toString()
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
                mockWebServer.url("/reactor/").toString()
            )
        }
    }
}