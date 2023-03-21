package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
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
}