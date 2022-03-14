package tv.mycujoo.e2e

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.exoplayer2.ExoPlayer
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import okhttp3.mockwebserver.MockWebServer
import tv.mycujoo.E2ETest
import tv.mycujoo.mcls.di.NetworkModuleBinds
import tv.mycujoo.mcls.di.PlayerModule

@HiltAndroidTest
@UninstallModules(NetworkModuleBinds::class, PlayerModule::class)
class LimitExceededWhenPlayingEvent : E2ETest() {

    @BindValue
    val context: Context = InstrumentationRegistry.getInstrumentation().context

    @BindValue
    val exoPlayer = ExoPlayer.Builder(context).build()



    companion object {
        val mockWebServer = MockWebServer()
    }

    @Module
    @InstallIn(SingletonComponent::class)
    class NetworkModule {
        fun provideConcurrencySocket() {

        }
    }
}