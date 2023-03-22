package tv.mycujoo.mcls.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import tv.mycujoo.mcls.manager.IPrefManager
import tv.mycujoo.mcls.manager.PrefManager
import tv.mycujoo.mcls.utils.UserPreferencesUtils
import javax.inject.Singleton

@Module(
    includes = [
        StorageModuleBinds::class,
        StorageModuleProvides::class
    ]
)
class StorageModule

@Module
interface StorageModuleBinds {
    @Binds
    @Singleton
    fun bindPrefManager(prefManager: PrefManager): IPrefManager
}

@Module
class StorageModuleProvides {

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
}