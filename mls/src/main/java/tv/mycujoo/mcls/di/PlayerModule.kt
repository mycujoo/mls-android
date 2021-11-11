package tv.mycujoo.mcls.di

import android.content.Context
import com.google.android.exoplayer2.MediaItem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.player.MediaFactory
import tv.mycujoo.mcls.player.Player
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PlayerModule {

    @Provides
    @Singleton
    fun provideMediaFactory(@ApplicationContext context: Context): MediaFactory {
        return MediaFactory(
            Player.createDefaultMediaSourceFactory(context),
            Player.createMediaFactory(context),
            MediaItem.Builder()
        )
    }
}