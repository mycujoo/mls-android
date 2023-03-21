package tv.mycujoo.mcls.di

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.npaw.ima.ImaAdapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.PrefManager
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.utils.ThreadUtils
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides 'App' related dependencies
 * Coroutines scope, Data manager & Pref manager are provided to dependency graph by this module
 */
@Module
open class AppModule {

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return Logger(LogLevel.MINIMAL)
    }

    @Provides
    @Singleton
    fun provideYouboraAdsAdapter(): ImaAdapter {
        return ImaAdapter()
    }

    @Provides
    @Singleton
    fun providePlugin(
        activity: FragmentActivity,
        @YouboraAccountCode accountCode: String,
        @DeviceType deviceType: String
    ): Plugin {
        val youboraOptions = Options()
        youboraOptions.accountCode = accountCode
        youboraOptions.isAutoDetectBackground = true

        youboraOptions.deviceCode = deviceType

        return Plugin(youboraOptions, activity.baseContext)
    }

    @Provides
    @Singleton
    @ExoPlayerOkHttp
    fun provideExoPlayerHttpClient(
        context: Context
    ): OkHttpClient {
        val cacheSize = 10 * 1024 * 1024 // 10 MiB
        val cache = Cache(context.cacheDir, cacheSize.toLong())

        val okHttpBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .cache(cache)

        return okHttpBuilder.build()
    }

    @Provides
    @Singleton
    fun providePrefManager(preferences: SharedPreferences): IPrefManager {
        return PrefManager(preferences)
    }

    @ObsoleteCoroutinesApi
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        val job = SupervisorJob()
        return CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesUtils(prefManager: IPrefManager): UserPreferencesUtils {
        return UserPreferencesUtils(prefManager)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences("MLS", Context.MODE_PRIVATE)
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
