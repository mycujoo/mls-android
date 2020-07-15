package tv.mycujoo.mls.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.network.MlsApi
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule(val context: Context) {

    private val maxAgeInSecond: Int = 60 * 5
    private val publicBaseUrl: String = "https://mls.mycujoo.tv"
    private val mlsApiBaseUrl: String = "https://mls-api.mycujoo.tv"

    @Provides
    @Singleton
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideOkHttp(prefManager: IPrefManager): OkHttpClient {

        val httpCacheDirectory = File(context.cacheDir, "responses")
        val cacheSize = 10 * 1024 * 1024 // 10 MiB

        val cache = Cache(httpCacheDirectory, cacheSize.toLong())

        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val newRequest = chain.request().newBuilder()
//                    .addHeader("Authorization", "Bearer " + prefManager.get("PUBLIC_KEY"))
                    .addHeader("Cache-Control", "public, max-age=$maxAgeInSecond")
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
            .cache(cache)

        return okHttpBuilder.build()
    }

    @Provides
    @Named("PUBLIC-API")
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(publicBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Named("MLS-API")
    @Singleton
    fun provideMlsApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(mlsApiBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

//    @Provides
//    @Singleton
//    fun providePublicApi(@Named("PUBLIC-API") retrofit: Retrofit): MlsApi {
//        return retrofit.create(MlsApi::class.java)
//    }

    @Provides
    @Singleton
    fun provideMlsApi(@Named("MLS-API") retrofit: Retrofit): MlsApi {
        return retrofit.create(MlsApi::class.java)
    }

}