package tv.mycujoo.mls.di

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import tv.mycujoo.domain.repository.EventsRepository
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.api.DataManager
import tv.mycujoo.mls.data.IDataManager
import tv.mycujoo.mls.enum.LogLevel
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.Logger
import tv.mycujoo.mls.manager.PrefManager
import javax.inject.Singleton

@Module
open class AppModule() {


    @Provides
    @Singleton
    open fun providePrefManager(context: Context): IPrefManager {
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