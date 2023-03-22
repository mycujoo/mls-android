package tv.mycujoo.mcls.di

import androidx.fragment.app.FragmentActivity
import com.npaw.ima.ImaAdapter
import com.npaw.youbora.lib6.plugin.Options
import com.npaw.youbora.lib6.plugin.Plugin
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AnalyticsModule {

    @Provides
    @Singleton
    fun providePlugin(
        activity: FragmentActivity,
        @YouboraAccountCode accountCode: String,
        @ClientDeviceType clientDeviceType: String
    ): Plugin {
        val youboraOptions = Options()
        youboraOptions.accountCode = accountCode
        youboraOptions.isAutoDetectBackground = true

        youboraOptions.deviceCode = clientDeviceType

        return Plugin(youboraOptions, activity.baseContext)
    }

    @Provides
    @Singleton
    fun provideImaAdapter() = ImaAdapter()

}