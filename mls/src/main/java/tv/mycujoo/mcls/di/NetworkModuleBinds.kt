package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.network.socket.ConcurrencySocket
import tv.mycujoo.mcls.network.socket.IConcurrencySocket
import tv.mycujoo.mcls.network.socket.IReactorSocket
import tv.mycujoo.mcls.network.socket.ReactorSocket
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface NetworkModuleBinds {



    @Binds
    @Singleton
    fun bindReactorSocket(ReactorSocket: ReactorSocket): IReactorSocket

    @Binds
    @Singleton
    fun bindConcurrencySocket(concurrencySocket: ConcurrencySocket): IConcurrencySocket
}