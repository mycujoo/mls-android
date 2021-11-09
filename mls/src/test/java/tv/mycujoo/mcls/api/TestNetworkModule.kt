package tv.mycujoo.mcls.api

import android.app.Activity
import android.content.Context
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.network.MlsApi

@TestInstallIn(
    replaces = [NetworkModule::class],
    components = [SingletonComponent::class]
)
@Module
class TestNetworkModule {

    var mockContext: Context = mock(Context::class.java)
    var mockOkHttpClient: OkHttpClient = mock(OkHttpClient::class.java)
    var mockRetrofit: Retrofit = mock(Retrofit::class.java)
    var mockMlsApi: MlsApi = mock(MlsApi::class.java)

    fun provideOkHttp(prefManager: IPrefManager): OkHttpClient {
        return mockOkHttpClient
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return mockRetrofit
    }

    fun provideMlsApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return mockRetrofit
    }

    fun provideMlsApi(retrofit: Retrofit): MlsApi {
        return mockMlsApi
    }
}