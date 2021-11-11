package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.manager.ViewHandler
import tv.mycujoo.mcls.manager.contracts.IViewHandler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ManagerModule {
    @Binds
    @Singleton
    fun bindViewHandler(viewHandler: ViewHandler): IViewHandler
}