package tv.mycujoo.mcls.api

import android.content.Context
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.mockito.Mockito.mock
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.manager.IPrefManager

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class TestAppModule {

    private var mockPrefManager: IPrefManager = mock(IPrefManager::class.java)
    private var mockCoroutineScope: CoroutineScope = mock(CoroutineScope::class.java)

    fun providePrefManager(context: Context): IPrefManager {
        return mockPrefManager
    }

    @ObsoleteCoroutinesApi
    fun provideCoroutineScope(): CoroutineScope {
        return mockCoroutineScope
    }
}
