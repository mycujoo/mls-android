package tv.mycujoo.mls.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule(val context: Context) {

    private val baseUrl: String = "https://mls.mycujoo.tv"

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val newRequest = chain.request().newBuilder()
//                    .addHeader("Authorization", "Bearer " + NetworkManager.getToken())
                    .build()
                val requestBody = newRequest.body()
                if (requestBody != null) {
                    Log.d(
                        "NetworkModule",
                        "intercept: " + chain.request().method() + " " + chain.request().url()
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
            })

        return okHttpBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}