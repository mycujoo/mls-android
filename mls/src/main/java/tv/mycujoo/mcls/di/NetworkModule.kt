package tv.mycujoo.mcls.di

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import tv.mycujoo.data.jsonadapter.JodaJsonAdapter
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.enum.C.Companion.PUBLIC_KEY_PREF_KEY
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.network.MlsApi
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides 'Network' related dependencies
 * API and Network clients are provided to dependency graph by this module
 */
@Module
open class NetworkModule {

    private val maxAgeInSecond: Int = 60 * 5

    @PublicBaseUrl
    @Provides
    @Singleton
    fun publicBaseUrl(): String = "https://mls.mycujoo.tv"

    @ApiBaseUrl
    @Provides
    @Singleton
    fun mlsApiBaseUrl(): String = "https://cda.mycujoo.tv"

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
        context: Context
    ): OkHttpClient {

        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain: Interceptor.Chain ->
                var authorizationHeader = "Bearer ${prefManager.get(PUBLIC_KEY_PREF_KEY)}"

//                if (prefManager.get(IDENTITY_TOKEN_PREF_KEY).isNullOrEmpty().not()) {
//                    authorizationHeader += ",${prefManager.get(IDENTITY_TOKEN_PREF_KEY)}"
//                }

                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", authorizationHeader)
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .addHeader("Cache-Control", "public, max-age=$maxAgeInSecond")
                    .addHeader("User-Agent", getUserAgent(context))
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
            .addInterceptor(loggingInterceptor)
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

    private fun getUserAgent(context: Context): String {
        val appVersion: Long = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(pInfo)
        } catch (e: Exception) {
            -1
        }

        // Check if TV
        return if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
            // Check if FireTV
            if (Build.MODEL.contains("AFT")) {
                "FireTV-AndroidAPI${Build.VERSION.SDK_INT}/${appVersion} SDK/${BuildConfig.SDK_VERSION}"
            } else {
                "AndroidTV-AndroidAPI${Build.VERSION.SDK_INT}/${appVersion} SDK/${BuildConfig.SDK_VERSION}"
            }
        } else {
            // Android Phone
            "Android-AndroidAPI${Build.VERSION.SDK_INT}/${appVersion} SDK/${BuildConfig.SDK_VERSION}"
        }
    }
}

