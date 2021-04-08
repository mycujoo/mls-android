package tv.mycujoo.mcls.api

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.mockito.Mockito.mock
import tv.mycujoo.mcls.di.AppModule
import tv.mycujoo.mcls.manager.IPrefManager

class TestAppModule() : AppModule() {

    var mockPrefManager: IPrefManager = mock(IPrefManager::class.java)
    var mockCoroutineScope: CoroutineScope = mock(CoroutineScope::class.java)

    override fun providePrefManager(context: Context): IPrefManager {
        return mockPrefManager
    }

    @ObsoleteCoroutinesApi
    override fun provideCoroutineScope(): CoroutineScope {
        return mockCoroutineScope
    }

}