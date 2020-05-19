package tv.mycujoo.mls.di

import android.app.Activity
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import tv.mycujoo.mls.BuildConfig
import javax.inject.Singleton

@Module
class AppModule() {

            @ObsoleteCoroutinesApi
    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(newSingleThreadContext(BuildConfig.LIBRARY_PACKAGE_NAME))
    }


}