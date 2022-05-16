package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.network.socket.BFFRTSocket
import tv.mycujoo.mcls.network.socket.IBFFRTSocket
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
    fun bindConcurrencySocket(BFFRTSocket: BFFRTSocket): IBFFRTSocket
}