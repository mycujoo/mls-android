package tv.mycujoo.mcls.api

import android.app.Activity
import android.content.Context
import okhttp3.OkHttpClient
import org.mockito.Mockito.mock
import retrofit2.Retrofit
import tv.mycujoo.mcls.di.NetworkModule
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.network.MlsApi

class TestNetworkModule(activity: Activity) : NetworkModule(activity) {

    var mockContext: Context = mock(Context::class.java)
    var mockOkHttpClient: OkHttpClient = mock(OkHttpClient::class.java)
    var mockRetrofit: Retrofit = mock(Retrofit::class.java)
    var mockMlsApi: MlsApi = mock(MlsApi::class.java)

    override fun provideContext(): Context {
        return mockContext
    }

    override fun provideOkHttp(prefManager: IPrefManager): OkHttpClient {
        return mockOkHttpClient
    }

    override fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return mockRetrofit
    }

    override fun provideMlsApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return mockRetrofit
    }

    override fun provideMlsApi(retrofit: Retrofit): MlsApi {
        return mockMlsApi
    }
}