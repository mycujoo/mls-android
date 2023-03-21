package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import tv.mycujoo.data.repository.EventsRepository
import tv.mycujoo.domain.repository.IEventsRepository
import tv.mycujoo.mcls.api.DataManager
import tv.mycujoo.mcls.data.IDataManager
import tv.mycujoo.mcls.network.socket.BFFRTSocket
import tv.mycujoo.mcls.network.socket.IBFFRTSocket
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.ReactorSocket
import javax.inject.Singleton

@Module
interface NetworkModuleBinds {

    @Binds
    @Singleton
    fun bindReactorSocket(ReactorSocket: ReactorSocket): IReactorSocket

    @Binds
    @Singleton
    fun bindConcurrencySocket(BFFRTSocket: BFFRTSocket): IBFFRTSocket

    @Binds
    @Singleton
    fun bindDataManager(dataManager: DataManager): IDataManager

    @Binds
    @Singleton
    fun bindEventsRepository(EventsRepository: EventsRepository): IEventsRepository
}