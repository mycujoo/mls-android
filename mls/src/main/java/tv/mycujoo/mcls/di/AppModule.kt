package tv.mycujoo.mcls.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mcls.BuildConfig
import tv.mycujoo.mcls.api.DataManager
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.Logger
import tv.mycujoo.mcls.manager.PrefManager
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
    open fun providePrefManager(@ApplicationContext context: Context): IPrefManager {
        return PrefManager(context.getSharedPreferences("MLS", Context.MODE_PRIVATE))
    }

    @ObsoleteCoroutinesApi
    @Provides
    @Singleton
    open fun provideCoroutineScope(): CoroutineScope {

        val job = SupervisorJob()

        return CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
    }

    @Provides
    @Singleton
    open fun provideDataManager(scope: CoroutineScope, repository: EventsRepository): IDataManager {
        return DataManager(scope, repository, Logger(LogLevel.MINIMAL))
    }
}
