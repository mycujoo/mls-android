package tv.mycujoo.mcls.di

import dagger.Module
import dagger.Provides
import tv.mycujoo.mcls.enum.LogLevel
import tv.mycujoo.mcls.manager.Logger
import javax.inject.Singleton

@Module(
    includes = [
        CoreModuleBinds::class,
        CoreModuleProvides::class
    ]
)
class CoreModule

@Module
interface CoreModuleBinds {

}

@Module
class CoreModuleProvides {
    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return Logger(LogLevel.MINIMAL)
    }
}