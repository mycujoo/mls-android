package tv.mycujoo.mcls.di

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tv.mycujoo.data.jsonadapter.JodaJsonAdapter
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
@InstallIn(SingletonComponent::class)
open class NetworkModule {

    private val maxAgeInSecond: Int = 60 * 5

    @PublicBaseUrl
    @Provides
    @Singleton
    fun publicBaseUrl(): String = "https://mls.mycujoo.tv"

    @ApiBaseUrl
    @Provides
    @Singleton
    fun mlsApiBaseUrl(): String = "https://mls-api.mycujoo.tv"

    @Provides
    @Singleton
    open fun provideOkHttp(prefManager: IPrefManager, @ApplicationContext context: Context): OkHttpClient {

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
        @PublicBaseUrl publicBaseUrl: String
    ): Retrofit {
        val moshi : Moshi = Moshi.Builder()
            .add(JodaJsonAdapter())
            .build()

        return Retrofit.Builder()
            .baseUrl(publicBaseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
    }

    @Provides
    @MLSAPI
    @Singleton
     fun provideMlsApiRetrofit(
        okHttpClient: OkHttpClient,
        @ApiBaseUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
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

private const val TAG = "NetworkModule"
