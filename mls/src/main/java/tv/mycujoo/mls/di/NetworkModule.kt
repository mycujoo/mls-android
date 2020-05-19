package tv.mycujoo.mls.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NetworkModule(val context: Context) {

    @Provides
    @Singleton
    fun provideContext() = context



}