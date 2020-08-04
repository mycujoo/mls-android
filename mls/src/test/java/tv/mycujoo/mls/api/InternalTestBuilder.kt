package tv.mycujoo.mls.api

import android.app.Activity
import org.mockito.Mockito.mock
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.di.DaggerTestMlsComponent
import tv.mycujoo.mls.manager.ViewIdentifierManager

class InternalTestBuilder(private val activity: Activity) : InternalBuilder(activity) {

    var mockViewIdentifierManager: ViewIdentifierManager = mock(ViewIdentifierManager::class.java)

    override fun initialize() {
        val appModule = TestAppModule()
        val networkModule = TestNetworkModule(activity)
        val repositoryModule = TestRepositoryModule()
        val dependencyGraph =
            DaggerTestMlsComponent.builder()
                .appModule(appModule)
                .networkModule(networkModule)
                .repositoryModule(repositoryModule)
                .build()
        dependencyGraph.inject(this)

        viewIdentifierManager = mockViewIdentifierManager
    }
}