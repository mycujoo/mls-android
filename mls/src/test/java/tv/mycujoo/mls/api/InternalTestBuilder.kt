package tv.mycujoo.mls.api

import android.app.Activity
import org.mockito.Mockito.mock
import tv.mycujoo.mls.core.InternalBuilder
import tv.mycujoo.mls.di.DaggerTestMlsComponent
import tv.mycujoo.mls.manager.ViewHandler
import tv.mycujoo.mls.manager.contracts.IViewHandler

class InternalTestBuilder(private val activity: Activity) : InternalBuilder(activity) {

    var mockViewHandler: IViewHandler = mock(ViewHandler::class.java)

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

        viewHandler = mockViewHandler
    }
}