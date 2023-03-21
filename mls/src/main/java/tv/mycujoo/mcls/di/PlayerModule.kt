package tv.mycujoo.mcls.di

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class PlayerModule {

    @Provides
    @Singleton
    fun provideExoPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build()
    }
}