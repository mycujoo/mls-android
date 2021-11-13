package tv.mycujoo.mcls.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.player.IPlayer
import tv.mycujoo.mcls.player.Player
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
interface PlayerModuleBinds {
    @Binds
    @Singleton
    fun bindPlayer(player: Player): IPlayer
}
