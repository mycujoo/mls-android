package tv.mycujoo.mcls.di

import android.content.Context
import android.content.res.AssetManager
import android.os.Handler
import android.os.Looper
import androidx.test.espresso.idling.CountingIdlingResource
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.PrefManager
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.utils.ThreadUtils
import javax.inject.Singleton

/**
 * Provides 'App' related dependencies
 * Coroutines scope, Data manager & Pref manager are provided to dependency graph by this module
 */
@Module
@InstallIn(SingletonComponent::class)
open class AppModule {

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return Logger(LogLevel.MINIMAL)
    }

    @Provides
    @Singleton
    fun providePrefManager(@ApplicationContext context: Context): IPrefManager {
        return PrefManager(context.getSharedPreferences("MLS", Context.MODE_PRIVATE))
    }

    @ObsoleteCoroutinesApi
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        val job = SupervisorJob()
        return CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
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
    fun provideHlsMediaSource(): HlsMediaSource.Factory {
        return HlsMediaSource.Factory(
            DefaultHttpDataSource.Factory()
        )
    }

    @Singleton
    @Provides
    fun provideDefaultMediaSourceFactory(
        @ApplicationContext context: Context
    ): DefaultMediaSourceFactory {
        return DefaultMediaSourceFactory(
            context
        )
    }

    @Singleton
    @Provides
    fun provideAssetManager(@ApplicationContext context: Context): AssetManager {
        return context.assets
    }
}
