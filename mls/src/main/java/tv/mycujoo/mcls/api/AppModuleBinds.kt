package tv.mycujoo.mcls.api

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.data.IDataManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModuleBinds {

    @Binds
    @Singleton
    fun bindDataManager(dataManager: DataManager): IDataManager
}