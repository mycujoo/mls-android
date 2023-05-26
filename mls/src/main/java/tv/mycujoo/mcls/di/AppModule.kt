package tv.mycujoo.mcls.di

import android.content.Context
import android.content.res.AssetManager
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import tv.mycujoo.mcls.enum.C
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.utils.ThreadUtils
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides 'App' related dependencies
 * Coroutines scope, Data manager & Pref manager are provided to dependency graph by this module
 */
@Module
class AppModule {

    @Provides
    @Singleton
    @ExoPlayerOkHttp
    fun provideExoPlayerHttpClient(
        context: Context,
        prefManager: IPrefManager
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())

        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .cache(cache)
            .addInterceptor { interceptor ->
                val request = interceptor.request().newBuilder()

                if(interceptor.request().url.toString().contains(".m3u8", true)) {
                    var authorizationHeader = "Bearer ${prefManager.get(C.PUBLIC_KEY_PREF_KEY)}"

                    if (prefManager.get(C.IDENTITY_TOKEN_PREF_KEY).isNullOrEmpty().not()) {
                        authorizationHeader += ",${prefManager.get(C.IDENTITY_TOKEN_PREF_KEY)}"
                    }

                    request.addHeader("Authorization", authorizationHeader)
                }

                interceptor.proceed(request.build())
            }

        return okHttpBuilder.build()
    }

    @CountingIdlingResourceViewIdentifierManager
    @Provides
    @Singleton
    fun provideViewIdentifierManagerCountingIdlingResources(): CountingIdlingResource {
        return CountingIdlingResource("ViewIdentifierManager")
    }

    @Provides
    @Singleton
    fun provideSchedulersUtil(): ThreadUtils {
        return ThreadUtils()
    }

    @Provides
    @Singleton
    fun provideMediaFactory(
        mediaSourceFactory: DefaultMediaSourceFactory,
        hlsMediaSource: HlsMediaSource.Factory
    ): MediaFactory {
        return MediaFactory(
            mediaSourceFactory,
            hlsMediaSource,
            MediaItem.Builder()
        )
    }

    @Singleton
    @Provides
    fun provideHlsMediaSource(
        @ExoPlayerOkHttp okHttpClient: OkHttpClient
    ): HlsMediaSource.Factory {
        return HlsMediaSource.Factory(
            OkHttpDataSource.Factory(okHttpClient)
        )
    }

    @Singleton
    @Provides
    fun provideDefaultMediaSourceFactory(
         context: Context
    ): DefaultMediaSourceFactory {
        val httpDataSourceFactory = DefaultHttpDataSource
            .Factory()
            .setAllowCrossProtocolRedirects(true)

        val dataSource = DefaultDataSource
            .Factory(context, httpDataSourceFactory)

        return DefaultMediaSourceFactory(dataSource)
    }

    @Singleton
    @Provides
    fun provideAssetManager( context: Context): AssetManager {
        return context.assets
    }
}
