package tv.mycujoo.mls.di

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext
import tv.mycujoo.mls.BuildConfig
import tv.mycujoo.mls.manager.IPrefManager
import tv.mycujoo.mls.manager.PrefManager
import javax.inject.Singleton

@Module
class AppModule() {


    @Provides
    @Singleton
    fun providePrefManager(context: Context): IPrefManager {
        return PrefManager(context.getSharedPreferences("MLS", Context.MODE_PRIVATE))
    }

    @ObsoleteCoroutinesApi
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {

        val job = SupervisorJob()

        return CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME) + job)
    }


}