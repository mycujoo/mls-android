package tv.mycujoo.mcls.di

import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.network.MlsApi
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Provides 'Network' related dependencies
 * API and Network clients are provided to dependency graph by this module
 */
@Module
open class NetworkModule(val context: Context) {

    private val maxAgeInSecond: Int = 60 * 5
    private val publicBaseUrl: String = "https://mls.mycujoo.tv"
    private val mlsApiBaseUrl: String = "https://mls-api.mycujoo.tv"

    @Provides
    @Singleton
    open fun provideContext() = context

    @Provides
    @Singleton
    open fun provideOkHttp(prefManager: IPrefManager): OkHttpClient {

        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())

        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain: Interceptor.Chain ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + prefManager.get(PUBLIC_KEY_PREF_KEY))
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .addHeader("Cache-Control", "public, max-age=$maxAgeInSecond")
                    .build()
                val requestBody = chain.request().body()
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
            }
            .cache(cache)

        return okHttpBuilder.build()
    }

    @Provides
    @Named("PUBLIC-API")
    @Singleton
    open fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(publicBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Named("MLS-API")
    @Singleton
    open fun provideMlsApiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(mlsApiBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    open fun provideMlsApi(@Named("MLS-API") retrofit: Retrofit): MlsApi {
        return retrofit.create(MlsApi::class.java)
    }

}