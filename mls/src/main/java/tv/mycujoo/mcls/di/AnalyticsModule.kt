package tv.mycujoo.mcls.di

import android.content.Context
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import tv.mycujoo.mcls.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AnalyticsModule {

    @Provides
    @Singleton
    fun provideYouboraConfig(): Options {
        val youboraOptions = Options()
        youboraOptions.accountCode = BuildConfig.MYCUJOO_YOUBORA_ACCOUNT_NAME
        youboraOptions.isAutoDetectBackground = true

        return youboraOptions
    }

    @Provides
    @Singleton
    fun provideYouboraPlugin(options: Options, @ApplicationContext context: Context): Plugin {
        return Plugin(options, context)
    }
}