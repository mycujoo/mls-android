package tv.mycujoo.mcls.di

import android.content.Context
import com.squareup.moshi.Moshi
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
import timber.log.Timber
import tv.mycujoo.data.jsonadapter.JodaJsonAdapter
import tv.mycujoo.mcls.enum.C.Companion.IDENTITY_TOKEN_PREF_KEY
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.network.MlsApi
import tv.mycujoo.mcls.network.NoConnectionInterceptor
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
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

    @ConcurrencySocketUrl
    @Provides
    @Singleton
    fun provideConcurrencySocketUrl(): String = "wss://bff-rt.mycujoo.tv"

    @ReactorUrl
    @Provides
    @Singleton
    fun provideReactorSocketUrl(): String = "wss://mls-rt.mycujoo.tv"

    @Provides
    @Singleton
    open fun provideOkHttp(
        prefManager: IPrefManager,
        @ApplicationContext context: Context,
        noConnectionInterceptor: NoConnectionInterceptor
    ): OkHttpClient {

        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())

        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(noConnectionInterceptor)
            .addInterceptor { chain: Interceptor.Chain ->
                var authorizationHeader = "Bearer ${prefManager.get(PUBLIC_KEY_PREF_KEY)}"

                if (prefManager.get(IDENTITY_TOKEN_PREF_KEY).isNullOrEmpty().not()) {
                    authorizationHeader += ",${prefManager.get(IDENTITY_TOKEN_PREF_KEY)}"
                }

                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", authorizationHeader)
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .addHeader("Cache-Control", "public, max-age=$maxAgeInSecond")
                    .build()
                val requestBody = chain.request().body
                if (requestBody != null) {
                    Timber.d(
                        "intercept: ${chain.request().method} ${chain.request().url}"
                    )
                    val buffer = Buffer()
                    requestBody.writeTo(buffer)
                    val charset = Charset.forName("UTF-8")
                    val contentType = requestBody.contentType()
                    if (contentType != null) {
                        Timber.d("intercept: ${buffer.readString(charset)}")
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
        val moshi: Moshi = Moshi.Builder()
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

